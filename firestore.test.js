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

test("Unauthenticated user: cannot read clusters or events", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("clusters").get());
  await assertFails(unauthDb.collection("events").get());
  await assertFails(unauthDb.collection("neighborhoods").get());
  await assertFails(unauthDb.collection("activities").get());
});

test("Authenticated user: can read clusters and events", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("clusters").doc("cluster_1").set({
      id: "cluster_1",
      name: "Cascade Foothills Cluster",
      milestone: "Milestone 2",
      userId: BOB_UID,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("clusters").doc("cluster_1").get());
});

test("Authenticated user: can create event with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("events").doc("event_1").set({
      id: "event_1",
      title: "Nineteen Day Feast of Mashíyyat",
      category: "Nineteen Day Feast",
      userId: ALICE_UID,
      location: "Neighborhood Center",
      attendeesCount: 25,
    })
  );
});

test("Authenticated user: cannot create event with mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("events").doc("event_bad").set({
      id: "event_bad",
      title: "Holy Day Gathering",
      category: "Holy Day",
      userId: BOB_UID,
    })
  );
});

test("Cross-user isolation: Alice cannot update Bob's event", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("events").doc("bob_event").set({
      id: "bob_event",
      title: "Bob's Devotional",
      category: "Core Activity",
      userId: BOB_UID,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("events").doc("bob_event").update({
      title: "Hacked Devotional",
    })
  );
});

test("User profile: Alice can manage her profile, Bob cannot read Alice profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice Bahá'í",
      clusterName: "Cascade Foothills",
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});
