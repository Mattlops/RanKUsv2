const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- UNATHENTICATED ACCESS REJECTIONS ---

test("Unauthenticated user: cannot read users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
});

test("Unauthenticated user: cannot read scores", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("scores").get());
});

test("Unauthenticated user: cannot create score", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("scores").doc("score_1").set({
    userId: ALICE_UID,
    gameType: "termo",
    dateKey: "2026-10-15",
    score: 100,
    createdAt: new Date(),
  }));
});

// --- AUTHENTICATED USER PERMISSIONS ---

test("Authenticated user: can create their own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    nickname: "AliceGamer",
    createdAt: new Date(),
  }));
});

test("Authenticated user: cannot create profile for another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    userId: BOB_UID,
    nickname: "FakeBob",
    createdAt: new Date(),
  }));
});

test("Authenticated user: can submit their score", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("scores").doc(`${ALICE_UID}_termo_2026-10-15`).set({
    userId: ALICE_UID,
    gameType: "termo",
    dateKey: "2026-10-15",
    score: 85,
    details: "4 palavras em 6 tentativas",
    createdAt: new Date(),
  }));
});

test("Authenticated user: cannot submit score with score > 100", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("scores").doc(`${ALICE_UID}_termo_2026-10-15`).set({
    userId: ALICE_UID,
    gameType: "termo",
    dateKey: "2026-10-15",
    score: 150, // Invalid!
    createdAt: new Date(),
  }));
});

test("Authenticated user: cannot overwrite someone else's score", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("scores").doc(`${BOB_UID}_termo_2026-10-15`).set({
    userId: BOB_UID,
    gameType: "termo",
    dateKey: "2026-10-15",
    score: 100,
    createdAt: new Date(),
  }));
});

test("Authenticated user: can update reactions on activity", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("activities").doc("act_1").set({
      userId: BOB_UID,
      playerName: "Bob",
      gameName: "Globle",
      pointsEarned: 95,
      fireReactions: 0,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("activities").doc("act_1").update({
    fireReactions: 1,
  }));
});
