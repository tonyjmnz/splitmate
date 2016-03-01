//Lets require/import the HTTP module
var express = require('express');
var app = express();
var mysql = require('mysql');
var bodyParser = require('body-parser')
app.use( bodyParser.json() );       // to support JSON-encoded bodies
var uuid = require('node-uuid');

var conn = mysql.createConnection({
  host     : 'localhost',
  user     : 'root',
  password : '123456',
  database : 'splitmate'
});

//Lets define a port we want to listen to
const PORT=8001;

var server = app.listen(PORT, function () {
  var host = server.address().address;
  var port = server.address().port;

  console.log('Example app listening at http://%s:%s', host, port);
  conn.connect();
});


app.post("/login", function(req, res) {
  var response = {};
  var login = req.body;
  console.log(login);

  conn.query("SELECT u.id FROM users u WHERE u.email = ? and u.password = ?", [login.email, login.password],
    function(err, results) {
      if (err) throw err;

      if(results.length) {
        response.status = 'success';
        response.userId = results[0].id;
        response.message = 'logged_in';
      } else {
        response.status = 'error';
        response.message = 'wrong_email_or_password';
      }

      res.setHeader('Content-Type', 'application/json');
      console.log(response);
      res.send(JSON.stringify(response));
    });
});

app.post("/signup", function(req, res) {
  var response = {};
  var user = req.body;
  console.log(user);

  conn.query("INSERT INTO users SET ?", {email: user.email, password: user.password},
    function(err, result) {
      if (err) {
        response.status = 'error';
        response.message = 'user_exists';
      } else {
        response.status = 'success';
        response.userId = result.insertId;
        response.message = 'user_created';
      }

      res.setHeader('Content-Type', 'application/json');

      console.log(response);
      res.send(JSON.stringify(response));
    });
});

app.post("/splitgroup", function(req, res) {
  var response = {};
  var splitgroup = req.body;
  console.log(splitgroup);

  //create splitgroup
  conn.query("INSERT INTO splitgroups SET ?", {name: splitgroup.groupName, created_by: splitgroup.creatorId},
    function(err, groupResult) {
      if (err) throw err;

      //insert the creator to the new group
      conn.query('INSERT INTO users_splitgroups SET ?', {id_user: splitgroup.creatorId, id_splitgroup: groupResult.insertId},
        function(err, result) {
          if(err) throw err;
      });

      if (splitgroup.members.length === 0) {
          response.status = 'success';
          response.message = 'no_members';

          res.setHeader('Content-Type', 'application/json');
          console.log(response);
          res.send(JSON.stringify(response));
          return;
      }

      //check which of the group members is already in the app and which ones need to be invited to the app
      conn.query('SELECT u.id, u.email FROM users u WHERE u.email IN (?)', [splitgroup.members], function(err, currentMembers) {
        if(err) throw err;

        var currentMembersEmails = currentMembers.map(function(row) {
          return row.email;
        });

        var currentMemberIds = currentMembers.map(function(row) {
          return row.id;
        });

        var appInvites = [];

        splitgroup.members.map(function(email) {
          if (currentMembersEmails.indexOf(email) != 0) {
            appInvites.push(email);
          }
        });

        var sendResponse = function() {
          response.status = 'success';
          response.message = 'group_created';

          res.setHeader('Content-Type', 'application/json');
          console.log(response);
          res.send(JSON.stringify(response));
        };

        //if we need to, send invite to members that have already signed up
        if (currentMemberIds.length) {
          insertGroupInvites(currentMemberIds, splitgroup.creatorId, groupResult.insertId, function() {
            if (!appInvites.length) {
              sendResponse();
            }
          });
        }

        //if the user doesn't exist invite to app
        if (appInvites.length) {
          insertAppInvites(appInvites, splitgroup.creatorId, groupResult.insertId, sendResponse);
        }
      });
    });
});

var insertGroupInvites = function (memberIds, inviterId, groupId, callback) {
  var invitationInsert = memberIds.map(function(id) {
    return {id_user: id, id_inviter: inviterId, id_group: groupId};
  });
  invitationInsert = getBulkInsert(invitationInsert);
  conn.query('INSERT INTO group_invites ' + invitationInsert.columns + ' VALUES ?', [invitationInsert.data], function(err, result) {
    if(err) throw err;
    callback();
  });
};

var insertAppInvites = function(emailsArr, inviterId, groupId, callback) {
  var appInvites = emailsArr.map(function(email) {
    return {
      email: email,
      key: uuid.v4(),
      invited_by: inviterId,
      id_splitgroup: groupId
    };
  });

  appInvites = getBulkInsert(appInvites);

  //send invite to members that aren't yet in the app
  conn.query('INSERT INTO app_invites ' + appInvites.columns + ' VALUES ?', [appInvites.data], function(err, result) {
    if (err) throw err;
    callback(result);
  });
};

app.get("/user/splitgroups/:userId", function(req, res) {
  var response = {};
  var userId = req.params.userId;

  getUserGroups(userId, function(results) {
    response.status = 'success';
    response.userGroups = results;
    response.message = '';

    res.setHeader('Content-Type', 'application/json');
    console.log(response);
    res.send(JSON.stringify(response));
  });
});

app.get("/splitgroup/:groupId", function(req, res) {
  var response = {};
  var groupId = req.params.groupId;
  conn.query("SELECT u.id, u.email FROM users u LEFT JOIN users_splitgroups us ON u.id = us.`id_user` WHERE us.id_splitgroup = ?", groupId,
    function(err, results) {
      if (err) throw err;
      response.status = 'success';
      response.message = '';
      response.members = results;

      res.setHeader('Content-Type', 'application/json');
      console.log(response);
      res.send(JSON.stringify(response));
    })
});

var getBalance = function(userId, groupId, callback) {
  conn.query("SELECT \
    IFNULL((SELECT ROUND(SUM(e.`amount`),2) as credit FROM expenses e WHERE e.id_user = ? and e.id_splitgroup = ? group by e.id_user) - \
    (SELECT ROUND(SUM(es.amount),2) FROM expenses e LEFT JOIN expense_split es ON e.id = es.id_expense WHERE es.id_user = ? AND e.id_splitgroup = ?), 0) as balance", [userId, groupId, userId, groupId],
    function(err, results) {
      if (err) throw err;
      callback(results);
    });
}

app.post("/expenses", function(req, res) {
  var response = {}
  var userId = req.body.userId;
  var groupId = req.body.groupId;

  getBalance(userId, groupId, function(results) {
    response.balance = results[0].balance;

    conn.query("SELECT e.id, e.description, ROUND(IFNULL(paid.amount, 0) - es.amount, 2) as balance \
      FROM expense_split es\
      LEFT JOIN (\
        SELECT e.amount, e.`description`, e.`id_user`, e.id FROM expenses e) AS paid\
      ON es.`id_user` = paid.id_user AND paid.id = es.id_expense\
      LEFT JOIN expenses e ON es.id_expense = e.id\
      WHERE es.id_user = ? and e.id_splitgroup = ?\
      ORDER BY e.id DESC", [userId, groupId], function(err, results) {
        if (err) throw err;
        response.detailedBalance = results;
        response.status = 'success';
        response.message = '';

        res.setHeader('Content-Type', 'application/json');
        console.log(response);
        res.send(JSON.stringify(response));
      });
    });
});

app.post('/payment', function(req,res) {
  var response = {};
  var insertData = {
    id_payer: req.body.payerId,
    id_payee: req.body.payeeId,
    id_splitgroup: req.body.groupId,
    amount: req.body.amount,
    desc: req.body.desc,
    date: new Date(),
  };

  conn.query("INSERT INTO payments SET ?", insertData, function(err, results) {
    if (err) throw err;
    response.status = 'success';
    response.message = 'payment_saved';

    res.setHeader('Content-Type', 'application/json');
    console.log(response);
    res.send(JSON.stringify(response));
  });
});

app.post('/balance', function(req, res) {
  var response = {};
  var userId = req.body.userId;
  var groupId = req.body.groupId;

  getBalance(userId, groupId, function(results) {
    response.balance = results[0].balance;
    response.status = 'success';
    response.message = '';

    res.setHeader('Content-Type', 'application/json');
    console.log(response);
    res.send(JSON.stringify(response));
  });
});

app.post("/expense", function(req,res) {
  console.log(req.body);
  var expense = req.body;
  /* { amount: '1423.0',
  userIds: [ 1, 11, 14 ],
  groupId: '75',
  amounts: [ 474.33, 474.33, 474.33 ],
  description: 'expense de prueb',
  userId: '1' } */
  var expenseData = {
    id_splitgroup: expense.groupId,
    id_user: expense.userId,
    description: expense.description,
    amount: expense.amount,
    date: new Date()};

  conn.query("INSERT INTO expenses SET ?", expenseData, function(err, result) {
    if (err) throw err;
    var expenseId = result.insertId;
    var insertData = expense.userIds.map(function(userId, index) {
      return {
        id_expense: expenseId,
        id_user: userId,
        amount: expense.amounts[index],
      };
    });
    console.log(insertData);
    insertData = getBulkInsert(insertData);
    conn.query('INSERT INTO expense_split ' + insertData.columns + ' VALUES ?', [insertData.data], function(err, result) {
      if (err) throw err;
      response.status = 'success';
      response.message = 'created_expense';

      res.setHeader('Content-Type', 'application/json');
      console.log(response);
      res.send(JSON.stringify(response));
    });
  });

});

app.get("/expense/:expenseId", function(req, res) {
  var response = {};
  var expenseId = req.params.expenseId;
  conn.query("SELECT u.email, e.amount FROM expenses e LEFT JOIN users u ON e.id_user = u.id WHERE e.id = ?", expenseId,
    function(err, result) {
      if(err) throw err;
      response.payer = result[0];
      conn.query("SELECT u.email, es.amount FROM expense_split es LEFT JOIN users u ON es.id_user = u.id \
        WHERE es.id_expense = ? && u.id != (SELECT e.id_user FROM expenses e WHERE e.id = ?)", [expenseId, expenseId],
        function(err, result) {
          if (err) throw err;
          response.owers = result;
          response.status = 'success';
          response.message = '';

          res.setHeader('Content-Type', 'application/json');
          console.log(response);
          res.send(JSON.stringify(response));
        });
    });
});



app.post("/splitgroup/invite", function(req, res) {
  var response = {};
  var invite = req.body;
  conn.query('SELECT u.id FROM users u WHERE u.email = ?', invite.email,
    function(err, results) {
      if (err) throw err;

      var sendResponse = function() {
        response.status = 'success';
        response.message = 'invited_member';

        res.setHeader('Content-Type', 'application/json');
        console.log(response);
        res.send(JSON.stringify(response));
      }

      if (results.length) {
        //send group invite
        insertGroupInvites([results[0].id], invite.inviterId, invite.groupId, sendResponse);
      } else {
        //send app invite
        console.log(invite);
        insertAppInvites([invite.email], invite.inviterId, invite.groupId, sendResponse);
      }

    })
});

var getUserGroups = function(userId, callback) {
  conn.query(
    'SELECT s.id, s.name FROM splitgroups s LEFT JOIN users_splitgroups us on s.id = us.`id_splitgroup` WHERE us.`id_user` = ?',
    userId, function(err, results){
      if(err) throw err;
      callback(results);
  });
};

var getBulkInsert = function(data) {
  if (!data[0]) return;

  insert = {};
  var columnNames = Object.keys(data[0]);

  insert.columns = '(`' + columnNames.join('`,`') + '`)';
  insert.data = data.map(function(record) {
    return columnNames.map(function(col) {
      return record[col];
    });
  });
  return insert;
};
