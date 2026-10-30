const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const PARTNER = {
  "R0xCX48rkDa9VyyK7tDkyyH5qIk1": "lGnuAexcJzaJLMsIjYKeR4bzyXJ3",
  "lGnuAexcJzaJLMsIjYKeR4bzyXJ3": "R0xCX48rkDa9VyyK7tDkyyH5qIk1"
};

exports.notifyPartnerOnChatMessage = onDocumentCreated(
  { document: "chat/{messageId}", region: "asia-south1" },
  async (event) => {
    const message = event.data?.data();
    if (!message || !message.uid || !message.text) return;
    const recipientUid = PARTNER[message.uid];
    if (!recipientUid) return;

    const db = getFirestore();
    const tokenSnap = await db.collection("pushTokens")
      .where("uid", "==", recipientUid).get();
    const tokens = [...new Set(tokenSnap.docs.map(d => d.get("token")).filter(Boolean))];
    if (!tokens.length) return;

    const senderName = message.name || "Your person";
    const body = String(message.text).slice(0, 180);
    const response = await getMessaging().sendEachForMulticast({
      tokens,
      notification: { title: `Our Little Life ♡ · ${senderName}`, body },
      data: {
        title: `Our Little Life ♡ · ${senderName}`,
        body,
        messageId: event.params.messageId,
        type: "chat_message"
      },
      android: {
        priority: "high",
        notification: {
          channelId: "our_little_life_messages",
          priority: "high",
          sound: "default",
          tag: event.params.messageId
        }
      }
    });

    const batch = db.batch();
    response.responses.forEach((result, index) => {
      if (!result.success && result.error?.code === "messaging/registration-token-not-registered") {
        batch.delete(tokenSnap.docs.find(d => d.get("token") === tokens[index]).ref);
      }
    });
    if (response.successCount > 0) {
      batch.update(event.data.ref, { dAt: Date.now(), pushSentAt: Date.now() });
    }
    await batch.commit();
  }
);