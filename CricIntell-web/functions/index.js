const functions = require('firebase-functions');
const express = require('express');
const engines = require('consolidate');
const admin = require('firebase-admin');
admin.initializeApp({
    credential: admin.credential.cert(require('./crickintelligence-firebase-adminsdk-1vygd-b0f95404b0.json'))
});
const db = admin.firestore();
db.settings({ timestampsInSnapshots: true });

const teams = ['Pakistan', 'India', 'England', 'Australia', 'West Indies', 'South Africa', 'New Zealand', 'Sri Lanka', 'Bangladesh', 'Zimbabwe', 'Kenya', 'Afghanistan', 'Hong Kong', 'Netherlands', 'Canada', 'Ireland', 'Nepal', 'Bermuda'];
const app = express();
app.engine('hbs', engines.handlebars);
app.set('views', './views');
app.set('view engine', 'hbs');
app.get('/', (request, response) => {
    response.render('index');
});

app.get('/teams/odi', (request, response) => {
    const data = require("./odi-matches.json");
    const collectionKey = "ODIMatches";

    if (data && (typeof data === "object")) {
        Object.keys(data).forEach(docKey => {
            db.collection(collectionKey).doc(docKey).set(data[docKey]).then((res) => {
                console.log("Document " + docKey + " successfully written!");
            }).catch((error) => {
                //console.error("Error writing document: ", error);
            });
        });
        response.send('Data imported');
   
 }
});

app.get('/grounds', (request, response) => {
    const data = require("./grounds.json");
    const collectionKey = "Grounds";

    if (data && (typeof data === "object")) {
        Object.keys(data).forEach(docKey => {
            db.collection(collectionKey).doc(docKey).set(data[docKey]).then((res) => {
                console.log("Document " + docKey + " successfully written!");
            }).catch((error) => {
                //console.error("Error writing document: ", error);
            });
        });
        response.send('Data imported');
   
 }
});
app.get('/teams', (request, response) => {
    const data = require("./players.json");
    const collectionKey = "Teams";

    if (data && (typeof data === "object")) {
        Object.keys(data).forEach(docKey => {
            db.collection(collectionKey).doc(data[docKey]["Team"]).collection("Players").add(data[docKey]).then((res) => {
                console.log("Document " + docKey + " successfully written!");
            }).catch((error) => {
                //console.error("Error writing document: ", error);
            });
        });
        response.send('Data imported');
   
 }
});


app.get('/players/odi', (request, response) => {
    const data = require("./odi-player-innings.json");
    const collectionKey = "ODIPlayers";
    if (data && (typeof data === "object")) {
        Object.keys(data).forEach(docKey => {
            console.log(docKey);
             db.collection(collectionKey).doc(docKey).set(data[docKey]).then((res) => {
                 console.log("Document " + docKey + " successfully written!");
             }).catch((error) => {

             });
        });
        response.send('');
    }
});
app.get('/teams/t20', (request, response) => {
    const data = require("./matches-t20.json");
    const collectionKey = "T20Matches";

    if (data && (typeof data === "object")) {
        Object.keys(data).forEach(docKey => {
            db.collection(collectionKey).doc(docKey).set(data[docKey]).then((res) => {
                console.log("Document " + docKey + " successfully written!");
            }).catch((error) => {
                //console.error("Error writing document: ", error);
            });
        });
        response.send('T-20 Data imported');
    }
});
app.get('/test', (request, response) => {
    db.collection('TestMatches').get().then((documents) => {
        documents.forEach((doc, index )=> {
            console.log(doc.id);
        });
        
    }).catch(err=>{
        console.log(`error:${err}`);
    });
    response.send('okay report');
});
app.get('/teams/test', (request, response) => {
    const data = require("./matches-test.json");
    const collectionKey = "TestMatches";

    if (data && (typeof data === "object")) {
        Object.keys(data).forEach(docKey => {
            db.collection(collectionKey).doc(docKey).set(data[docKey]).then((res) => {
                console.log("Document " + docKey + " successfully written!");
            }).catch((error) => {
                //console.error("Error writing document: ", error);
            });
        });
        response.send('Data imported');
    }
});
getOpponent = (match, country) => {
    const opponents = match.split(' v ');
    return opponents[0] == country ? opponents[1] : opponents[0];
}

exports.app = functions.https.onRequest(app);
