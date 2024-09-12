var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
  next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;

var admin = require('firebase-admin');
var request = require('request');
var { google } = require('googleapis');

// fetch the service account key JSON file contents
var serviceAccount = require("");
var API_KEY = '';

// app initialisation
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: ""
});


// ----- INITIALISATION ----- //


var realtimeDB = admin.database();
var firestore = admin.firestore();

const fs = require('fs');
const readline = require('readline');
const sentimentWords = {};

//read the afinn list
const sentimentList = readline.createInterface({
  input: fs.createReadStream('afinn_list_en165.txt'),
  output: process.stdout,
  terminal: false
});

//break the afinn list into words and equivalent score
sentimentList.on('line', (line) => {
  const [word, score] = line.split(/\s+/);
  sentimentWords[word] = parseFloat(score);
});

sentimentList.on('close', () => {
  console.log('Sentiment words loaded:', sentimentWords);
});

// sentiment analysis with natural library
const natural = require('natural');
const Analyser = natural.SentimentAnalyzer;
const stemmer = natural.PorterStemmer;
const analyser = new Analyser("English", stemmer, "afinn");


// ----- FUNCTIONS ----- //

// function that breaks the messages into tokens - my version
function myTokeniser(message) {
  const cleanMessage = message.toLowerCase().replace(/[^a-z\s]/g, '');
  return cleanMessage.split(/\s+/);
}

// function that does sentiment analysis - my own version
function mySentimentAnalysis(message) {
  const tokens = myTokeniser(message);
  let score = 0;
  let counter = 0;
  tokens.forEach(token => {
    if (sentimentWords.hasOwnProperty(token)) {
      score += sentimentWords[token];
      counter++;
    } else {
      score += 0;
    }
  });

  if (counter > 0) {
    score = score / counter;
  } else {
    score = 0;
  }
  console.log('Calculated score:', score);
  return score;
}

// function that does the sentiment analysis with the natural library
function sentimentAnalysisNatural(message) {
  const tokeniser = new natural.WordTokenizer();
  const tokens = tokeniser.tokenize(message);
  const score = analyser.getSentiment(tokens);

  return score;
}

// function that changes the colour values in the realtime db
function setColourValues(colour) {
  const deviceRef = realtimeDB.ref('devices/esp32wroomDA9826/colour');
  deviceRef.set({
    R: colour.red,
    G: colour.green,
    B: colour.blue
  }).then(() => {
    console.log('Colours updated to Firebase');
  }).catch((error) => {
    console.error('Error pushing colours to Firebase:', error);
  });
}

// function that maps sentiment score to colour values
function mapScoreToColour(score) {
  let red, green, blue;

  if (score <= -0.8) {
    // blue
    red = 21;
    green = 5;
    blue = 255;
  } else if (score <= -0.6 && score > -0.8) {
    // risd blue
    red = 13;
    green = 82;
    blue = 251;
  } else if (score <= -0.4 && score > -0.6) {
    // celestial blue
    red = 5;
    green = 158;
    blue = 247;
  } else if (score <= -0.2 && score > -0.4) {
    // turquoise
    red = 5;
    green = 203;
    blue = 179;
  } else if (score < 0 && score > -0.2) {
    // spring green
    red = 5;
    green = 247;
    blue = 110;
  } else if (score <= 0.2 && score > 0) {
    // lawn green
    red = 126;
    green = 245;
    blue = 58;
  } else if (score <= 0.4 && score > 0.2) {
    // lime
    red = 187;
    green = 244;
    blue = 32;
  } else if (score <= 0.6 && score > 0.4) {
    // aurelion
    red = 247;
    green = 243;
    blue = 5;
  } else if (score <= 0.8 && score > 0.6) {
    // cyclamen
    red = 255;
    green = 102;
    blue = 153;
  } else if (score > 0.8) {
    // amethyst
    red = 134;
    green = 92;
    blue = 202;
  } else {
    // white
    red = 255;
    green = 255;
    blue = 255;
  }

  return { red, green, blue };
}

// function that gets the current connected user to my device
async function getCurrentUser() {
  const connectedUserRef = realtimeDB.ref('devices/esp32wroomDA9826/connectedUser');
  try {
    const snapshot = await connectedUserRef.once('value');
    const userData = snapshot.val();
    console.log('Fetched current user data:', userData);
    const userId = userData.userId;
    console.log('Extracted user ID:', userId);
    return userId;
  } catch (error) {
    console.error('Error fetching current user:', error);
    return null;
  }
}

// function that gets the messages of the current connected user
async function fetchSenderMessages(userId) {
  if (!userId) {
    console.log('No user ID provided');
    return [];
  }

  if (typeof userId !== 'string') {
    console.error('User ID is not a string:', userId);
    return [];
  }

  try {
    const messages = [];
    const chatsRef = firestore.collection(`users/${userId}/chats`);
    const querySnapshot = await chatsRef.where('messageType', '==', 'sender').get();

    if (querySnapshot.empty) {
      console.log(`No sender messages found for user: ${userId}`);
      return [];
    }

    querySnapshot.forEach(doc => {
      messages.push({ id: doc.id, ...doc.data() });
    });

    messages.sort((a, b) => parseInt(a.id) - parseInt(b.id));

    console.log('Filtered and sorted sender messages:', messages);
    return messages;
  } catch (error) {
    console.error('Error fetching sender messages:', error);
    return [];
  }
}

// function that adds the sentiment analysis for each new message
function sentimentAnalysisNewMessages(userId) {
  const chatsRef = firestore.collection(`users/${userId}/chats`);
  chatsRef.onSnapshot(snapshot => {
    snapshot.docChanges().forEach(change => {
      if (change.type === 'added') {
        const doc = change.doc;
        const chatId = doc.id;
        const chatData = doc.data();
        if (!chatData.sentAnScore) {
          const message = chatData.message;
          const score = mySentimentAnalysis(message);
          //const score = SentimentAnalysisNatural(message);
          const chatDocRef = chatsRef.doc(chatId);
          chatDocRef.update({ sentAnScore: score.toString() })
            .then(() => {
              console.log(`Sentiment analysis added to new chat message ${chatId}`);
            })
            .catch(error => {
              console.error(`Error adding sentiment analysis to new chat message ${chatId}:`, error);
            });
          // set the colours to LED for a specific score  
          if (chatData.messageType === 'sender') {
            setColourValues(mapScoreToColour(score));
          }
        }
      }
    });
  });
}

// ----- FUNCTION CALLS ----- // 

getCurrentUser().then(userId => {
  console.log('Connected user:', userId);
  if (userId) {
    fetchSenderMessages(userId).then(messages => {
      console.log('Sender messages:', messages);
    });
    sentimentAnalysisNewMessages(userId);
  } else {
    console.log('No user connected or error occurred.');
  }
});