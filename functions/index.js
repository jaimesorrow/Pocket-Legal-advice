/**
 * Alaska's Pocket Lawbook — subscription entitlement backend.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * NOT YET DEPLOYED. This is written to be deployable once a Firebase project
 * and a Play Console account exist. It cannot be run or tested from the repo
 * alone — it needs a service account with Play Developer API access, a
 * published subscription product, and a Pub/Sub topic wired to Play's
 * real-time developer notifications.
 *
 * WHY THIS EXISTS AT ALL: the Android client must never be the authority on
 * whether someone has paid. The client can only ever say "a purchase flow
 * completed here", which is trivially faked by patching the APK. This service
 * validates the purchase token with Google directly and records entitlement
 * against the user's uid, and gated content is served only against that record.
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Deploy:  firebase deploy --only functions
 */

const functions = require("firebase-functions/v2");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onMessagePublished } = require("firebase-functions/v2/pubsub");
const admin = require("firebase-admin");
const { google } = require("googleapis");

admin.initializeApp();
const db = admin.firestore();

const PACKAGE_NAME = "com.pocketlawbook.alaska";
const SUBSCRIPTION_PRODUCT_ID = "pocket_lawbook_monthly";

/** Play subscription states that mean "let them in". */
const ENTITLING_STATES = new Set([
  1, // SUBSCRIPTION_STATE_ACTIVE
  2, // SUBSCRIPTION_STATE_CANCELED — paid through the end of the period
  4, // SUBSCRIPTION_STATE_IN_GRACE_PERIOD — payment failing, still has access
]);

async function playApi() {
  const auth = new google.auth.GoogleAuth({
    scopes: ["https://www.googleapis.com/auth/androidpublisher"],
  });
  return google.androidpublisher({ version: "v3", auth: await auth.getClient() });
}

/**
 * Writes the entitlement record the app reads. This document is the single
 * source of truth for "may this user read case law".
 */
async function writeEntitlement(uid, { active, expiryMillis, purchaseToken, state }) {
  await db.collection("entitlements").doc(uid).set(
    {
      caseLaw: active,
      aiChat: active,
      subscriptionState: state,
      expiresAt: expiryMillis ? new Date(Number(expiryMillis)) : null,
      purchaseToken: purchaseToken || null,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );
}

/**
 * Called by the app right after a purchase completes locally, and again on
 * "restore purchases". Validates the token with Google before granting anything.
 */
exports.validatePurchase = onCall(async (request) => {
  const uid = request.auth && request.auth.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "Sign in before validating a purchase.");
  }

  const purchaseToken = request.data && request.data.purchaseToken;
  if (!purchaseToken) {
    throw new HttpsError("invalid-argument", "purchaseToken is required.");
  }

  let subscription;
  try {
    const api = await playApi();
    const res = await api.purchases.subscriptionsv2.get({
      packageName: PACKAGE_NAME,
      token: purchaseToken,
    });
    subscription = res.data;
  } catch (err) {
    functions.logger.error("Play validation failed", { uid, err: err.message });
    throw new HttpsError("internal", "Could not verify the purchase with Google Play.");
  }

  // Refuse a token that belongs to a different product.
  const lineItems = subscription.lineItems || [];
  const matchesProduct = lineItems.some((li) => li.productId === SUBSCRIPTION_PRODUCT_ID);
  if (!matchesProduct) {
    throw new HttpsError("failed-precondition", "That purchase is for a different product.");
  }

  // Refuse a token already bound to a different account, so one purchase
  // cannot unlock many accounts.
  const existing = await db
    .collection("entitlements")
    .where("purchaseToken", "==", purchaseToken)
    .get();
  const boundElsewhere = existing.docs.some((d) => d.id !== uid);
  if (boundElsewhere) {
    throw new HttpsError("already-exists", "That purchase is already linked to another account.");
  }

  const state = subscription.subscriptionState;
  const active = ENTITLING_STATES.has(
    typeof state === "number" ? state : Number(state)
  );
  const expiryMillis =
    lineItems.length > 0 && lineItems[0].expiryTime
      ? Date.parse(lineItems[0].expiryTime)
      : null;

  await writeEntitlement(uid, { active, expiryMillis, purchaseToken, state });

  // Acknowledge, or Play refunds the purchase automatically after 3 days.
  if (subscription.acknowledgementState === 1) {
    try {
      const api = await playApi();
      await api.purchases.subscriptions.acknowledge({
        packageName: PACKAGE_NAME,
        subscriptionId: SUBSCRIPTION_PRODUCT_ID,
        token: purchaseToken,
        requestBody: {},
      });
    } catch (err) {
      functions.logger.error("Acknowledge failed", { uid, err: err.message });
    }
  }

  return { entitled: active, expiresAt: expiryMillis };
});

/**
 * Play real-time developer notifications.
 *
 * Without this, entitlement drifts: a cancellation, refund, or failed renewal
 * would not reach the app until the user next signed in, so people would keep
 * access they no longer pay for and lose access they do.
 */
exports.playNotifications = onMessagePublished("play-billing-notifications", async (event) => {
  const raw = event.data.message.data;
  if (!raw) return;

  const payload = JSON.parse(Buffer.from(raw, "base64").toString("utf8"));
  const notification = payload.subscriptionNotification;
  if (!notification) return;

  const purchaseToken = notification.purchaseToken;
  const snapshot = await db
    .collection("entitlements")
    .where("purchaseToken", "==", purchaseToken)
    .limit(1)
    .get();

  if (snapshot.empty) {
    functions.logger.warn("Notification for unknown purchase token");
    return;
  }

  const uid = snapshot.docs[0].id;

  let subscription;
  try {
    const api = await playApi();
    const res = await api.purchases.subscriptionsv2.get({
      packageName: PACKAGE_NAME,
      token: purchaseToken,
    });
    subscription = res.data;
  } catch (err) {
    functions.logger.error("Play re-check failed", { uid, err: err.message });
    return;
  }

  const state = subscription.subscriptionState;
  const active = ENTITLING_STATES.has(
    typeof state === "number" ? state : Number(state)
  );
  const lineItems = subscription.lineItems || [];
  const expiryMillis =
    lineItems.length > 0 && lineItems[0].expiryTime
      ? Date.parse(lineItems[0].expiryTime)
      : null;

  await writeEntitlement(uid, { active, expiryMillis, purchaseToken, state });
  functions.logger.info("Entitlement updated from Play notification", { uid, active, state });
});

/**
 * Account deletion. Google Play requires an in-app path AND a web path for any
 * app that lets users create accounts.
 */
exports.deleteAccount = onCall(async (request) => {
  const uid = request.auth && request.auth.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "Sign in first.");
  }

  await db.collection("entitlements").doc(uid).delete();
  await admin.auth().deleteUser(uid);

  // Note: this does not cancel the Play subscription. The user cancels that in
  // Play, and the terms have to say so.
  return { deleted: true };
});
