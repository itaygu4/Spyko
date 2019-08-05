//import firebase functions modules
var functions = require('firebase-functions');
//import admin module
var admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


// Listens for new messages added to messages/:pushId
exports.pushNotification = functions.database.ref('/users/{pushId}').onWrite( event => {

    console.log('Push notification event triggered');
    //  Grab the current value of what was written to the Realtime Database.
    var valueObject = event.data.val();

    if (valueObject.live == 'true'){
        // Create a notification
        const payload = {
            notification: {
                title:"Spyko",
                body: "Is now live! Join him now!",
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };


        return admin.messaging().sendToTopic(pushId, payload, options);
    }

  
});