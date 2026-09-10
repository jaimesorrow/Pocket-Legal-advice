/**
 * Alaska's Pocket Lawbook — subscription entitlement backend.
 *
 * The Android client is never the legal-content authority. Legal source
 * monitoring and publication happen server-side.
 */

const functions = require("firebase-functions/v2");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onMessagePublished } = require("firebase-functions/v2/pubsub");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const { google } = require("googleapis");
const { inspectAllSources } = require("./legalIngestion");

admin.initializeApp();
const db = admin.firestore();

const PACKAGE_NAME = "com.pocketlawbook.alaska";
const SUBSCRIPTION_PRODUCT_ID = "pocket_lawbook_monthly";

const ENTITLING_STATES = new Set([1, 2, 4]);

async function playApi() {
  const auth = new google.auth.GoogleAuth({
    scopes: ["https://www.googleapis.com/auth/androidpublisher"],
  });
  return google.androidpublisher({ version: "v3", auth: await auth.getClient() });
}

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

exports.validatePurchase = onCall(async (request) => {
  const uid = request.auth && request.auth.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign in before validating a purchase.");

  const purchaseToken = request.data && request.data.purchaseToken;
  if (!purchaseToken) throw new HttpsError("invalid-argument", "purchaseToken is required.");

  let subscription;
  try {
    const api = await playApi();
    const res = await api.purchases.subscriptionsv2.get({ packageName: PACKAGE_NAME, token: purchaseToken });
    subscription = res.data;
  } catch (err) {
    functions.logger.error("Play validation failed", { uid, err: err.message });
    throw new HttpsError("internal", "Could not verify the purchase with Google Play.");
  }

  const lineItems = subscription.lineItems || [];
  if (!lineItems.some((li) => li.productId === SUBSCRIPTION_PRODUCT_ID)) {
    throw new HttpsError("failed-precondition", "That purchase is for a different product.");
  }

  const existing = await db.collection("entitlements").where("purchaseToken", "==", purchaseToken).get();
  if (existing.docs.some((d) => d.id !== uid)) {
    throw new HttpsError("already-exists", "That purchase is already linked to another account.");
  }

  const state = subscription.subscriptionState;
  const active = ENTITLING_STATES.has(typeof state === "number" ? state : Number(state));
  const expiryMillis = lineItems[0] && lineItems[0].expiryTime ? Date.parse(lineItems[0].expiryTime) : null;
  await writeEntitlement(uid, { active, expiryMillis, purchaseToken, state });

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

exports.playNotifications = onMessagePublished("play-billing-notifications", async (event) => {
  const raw = event.data.message.data;
  if (!raw) return;
  const payload = JSON.parse(Buffer.from(raw, "base64").toString("utf8"));
  const notification = payload.subscriptionNotification;
  if (!notification) return;

  const purchaseToken = notification.purchaseToken;
  const snapshot = await db.collection("entitlements").where("purchaseToken", "==", purchaseToken).limit(1).get();
  if (snapshot.empty) return;

  const uid = snapshot.docs[0].id;
  try {
    const api = await playApi();
    const res = await api.purchases.subscriptionsv2.get({ packageName: PACKAGE_NAME, token: purchaseToken });
    const subscription = res.data;
    const state = subscription.subscriptionState;
    const active = ENTITLING_STATES.has(typeof state === "number" ? state : Number(state));
    const lineItems = subscription.lineItems || [];
    const expiryMillis = lineItems[0] && lineItems[0].expiryTime ? Date.parse(lineItems[0].expiryTime) : null;
    await writeEntitlement(uid, { active, expiryMillis, purchaseToken, state });
  } catch (err) {
    functions.logger.error("Play re-check failed", { uid, err: err.message });
  }
});

exports.deleteAccount = onCall(async (request) => {
  const uid = request.auth && request.auth.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign in first.");
  await db.collection("entitlements").doc(uid).delete();
  await admin.auth().deleteUser(uid);
  return { deleted: true };
});

/**
 * Runs every day. It monitors only explicitly allow-listed authoritative legal
 * sources. A changed source is recorded as a pending change; it is NOT silently
 * promoted to user-facing legal text. Promotion requires the validation/content
 * publication pipeline to accept a versioned dataset.
 */
exports.dailyLegalSourceCheck = onSchedule(
  {
    schedule: "15 3 * * *",
    timeZone: "America/Anchorage",
    retryCount: 2,
    maxInstances: 1,
  },
  async () => {
    const startedAt = admin.firestore.Timestamp.now();
    const results = await inspectAllSources();
    const checked = results.filter((r) => r.status === "CHECKED");
    const failed = results.filter((r) => r.status === "FAILED");

    const previousSnapshot = await db.collection("legal_source_state").get();
    const previous = new Map(previousSnapshot.docs.map((d) => [d.id, d.data()]));
    const changes = [];

    for (const result of checked) {
      const old = previous.get(result.id);
      const changed = Boolean(old && old.contentHash && old.contentHash !== result.contentHash);

      await db.collection("legal_source_state").doc(result.id).set({
        ...result,
        previousHash: old ? old.contentHash || null : null,
        changed,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true });

      if (changed) {
        changes.push({
          sourceId: result.id,
          jurisdiction: result.jurisdiction,
          sourceType: result.sourceType,
          authority: result.authority,
          previousHash: old.contentHash,
          currentHash: result.contentHash,
          detectedAt: result.checkedAt,
          validationStatus: "PENDING_VALIDATION",
        });
      }
    }

    for (const change of changes) {
      await db.collection("legal_change_events").add({
        ...change,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    await db.collection("legal_update_runs").add({
      startedAt,
      completedAt: admin.firestore.FieldValue.serverTimestamp(),
      checkedCount: checked.length,
      failedCount: failed.length,
      changedCount: changes.length,
      failedSources: failed.map((r) => ({ id: r.id, error: r.error })),
      status: failed.length === results.length ? "FAILED" : "COMPLETED",
      publication: "NO_AUTOMATIC_TEXT_PROMOTION",
    });

    functions.logger.info("Daily legal source check completed", {
      checked: checked.length,
      failed: failed.length,
      changed: changes.length,
    });
  }
);
