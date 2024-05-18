// 生成uuid
function generateUUID() {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < 36; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }

    // 根据UUID规范设置必要的字符
    result = result.replace(/(.{8})(.{4})(.{4})(.{4})(.{12})/, "$1-$2-$3-$4-$5");

    return result;
}

function initSwitchTab() {
    let tabSession = document.querySelector('.tab .tab-session');
    let tabFriend = document.querySelector('.tab .tab-friend');
    let tabAdd = document.querySelector('.tab .tab-add');
    let lists = document.querySelectorAll('.list');
    tabSession.onclick = function () {
        tabSession.style.backgroundImage = 'url(img/session1.png)';
        tabFriend.style.backgroundImage = 'url(img/friend2.png)';
        lists[0].classList = 'list';
        lists[1].classList = 'list hide';
        lists[2].classList = 'list hide';
    }

    tabFriend.onclick = function () {
        tabSession.style.backgroundImage = 'url(img/session2.png)';
        tabFriend.style.backgroundImage = 'url(img/friend1.png)';
        lists[0].classList = 'list hide';
        lists[1].classList = 'list';
        lists[2].classList = 'list hide';
        getFriendList();
    }

    tabAdd.onclick = function () {
        tabSession.style.backgroundImage = 'url(img/session2.png)';
        tabFriend.style.backgroundImage = 'url(img/friend2.png)';
        lists[0].classList = 'list hide';
        lists[1].classList = 'list hide';
        lists[2].classList = 'list';
        let req = {
            type: 'select_invite',
        };
        req = JSON.stringify(req);
        console.log("[websocket] send: " + req);
        websocket.send(req);

        // 给搜索按钮添加事件
        let sendButton = document.querySelector('.search-friend button');
        let messageInput = document.querySelector('.search-friend .search-friend-text');
        // 给发送按钮注册一个点击事件
        sendButton.onclick = function () {
            if (!messageInput.value) {
                return;
            }
            let req = {
                type: 'searchFriends',
                content: messageInput.value
            };
            req = JSON.stringify(req);
            console.log("[websocket] send: " + req);
            websocket.send(req);
            messageInput.value = '';
        }
    }
}
initSwitchTab();

let websocket = new WebSocket("ws://" + location.host + "/WebSocketMessage");
websocket.onopen = function () {
    console.log("websocket 连接成功!");
}

websocket.onmessage = function (e) {
    console.log("websocket 收到消息! " + e.data);
    // 此时收到的 e.data 是个 json 字符串, 需要转成 js 对象
    let resp = JSON.parse(e.data);
    if (resp.type == 'message') {
        // 处理消息响应
        handleMessage(resp);
    } else if (resp.type == 'friends') {
        let list = document.querySelectorAll('.list')[2];
        for (let user of resp.users) {
            let li = document.createElement('li');
            li.innerHTML = '<h4>' + user.username + '</h4>';
            li.setAttribute('user-id', user.userId);
            list.appendChild(li);

            li.onclick = function () {
                addFriend(user);
            }
        }
    } else if (resp.type == 'select_success') {
        let list = document.querySelectorAll('.list')[2];
        for (let user of resp.users) {
            let li = document.createElement('li');
            li.innerHTML = '<h4>' + user.username + '</h4>';
            li.setAttribute('user-id', user.userId);
            list.appendChild(li);

            li.onclick = function () {
                select_success(user);
            }
        }
    }else if(resp.type == 'invite_error_myself'){
        alert('不可点击自己');
    }else if(resp.type == 'invite_error'){
        alert('该用户已是你的好友');
    }else if(resp.type == 'update_success'){
        location.reload(true);
    }else {
        // resp 的 type 出错!
        console.log("resp.type 不符合要求!");
    }
}

function select_success(user){
    if (confirm("是否同意该好友申请")){
        let req = {
            type: 'invite_success',
            content: user.username
        };
        req = JSON.stringify(req);
        console.log("[websocket] send: " + req);
        websocket.send(req);
        location.href = "client.html";
    }else{
        let req = {
            type: 'invite_fail',
            content: user.username
        };
        req = JSON.stringify(req);
        console.log("[websocket] send: " + req);
        websocket.send(req);
        location.href = "client.html";
    }
}

websocket.onclose = function () {
    console.log("websocket 连接关闭!");
}

websocket.onerror = function () {
    console.log("websocket 连接异常!");
}

// 收到消息的业务处理函数
function handleMessage(resp) {
    let curSessionLi = findSessionLi(resp.sessionId);
    if (curSessionLi == null) {
        curSessionLi = document.createElement('li');
        curSessionLi.setAttribute('message-session-id', resp.sessionId);
        curSessionLi.innerHTML = '<h3>' + resp.fromName + '</h3>'
            + '<p></p>';
        curSessionLi.onclick = function () {
            clickSession(curSessionLi);
        }
    }

    let p = curSessionLi.querySelector('p');
    p.innerHTML = resp.content;
    if (p.innerHTML.length > 10) {
        p.innerHTML = p.innerHTML.substring(0, 10) + '...';
    }

    let sessionListUL = document.querySelector('#session-list');
    sessionListUL.insertBefore(curSessionLi, sessionListUL.children[0]);

    if (curSessionLi.className == 'selected') {
        // 把消息列表添加一个新消息. 
        let messageShowDiv = document.querySelector('.right .message-show');
        addMessage(messageShowDiv, resp);
        scrollBottom(messageShowDiv);
    }

}

// 添加好友点击事件
function addFriend(user) {
    if (confirm("是否向该用户发起好友申请")) {
        let req = {
            type: 'invite_start',
            content: user.username
        };
        req = JSON.stringify(req);
        console.log("[websocket] send: " + req);
        websocket.send(req);
    }
}

// 获取到所有的会话列表中的 li 标签
function findSessionLi(targetSessionId) {
    let sessionLis = document.querySelectorAll('#session-list li');
    for (let li of sessionLis) {
        let sessionId = li.getAttribute('message-session-id');
        if (sessionId == targetSessionId) {
            return li;
        }
    }

    return null;
}

// 发送按钮的业务处理
function initSendButton() {
    let sendButton = document.querySelector('.right .ctrl button');
    let messageInput = document.querySelector('.right .message-input');
    // 给发送按钮注册一个点击事件
    sendButton.onclick = function () {
        if (!messageInput.value) {
            return;
        }

        let selectedLi = document.querySelector('#session-list .selected');
        if (selectedLi == null) {
            return;
        }
        let sessionId = selectedLi.getAttribute('message-session-id');
        let req = {
            type: 'message',
            sessionId: sessionId,
            content: messageInput.value
        };
        req = JSON.stringify(req);
        console.log("[websocket] send: " + req);
        websocket.send(req);
        messageInput.value = '';
    }
}
initSendButton();

// 获取用户信息
function getUserInfo() {
    $.ajax({
        type: 'get',
        url: 'userInfo',
        success: function (body) {
            if (body.userId && body.userId > 0) {
                let userDiv = document.querySelector('.main .left .user-head');

                userDiv.innerHTML = "<div class='user'>" + body.username + "</div>";
                userDiv.setAttribute("user-id", body.userId);
            } else {
                alert("当前用户未登录!");
                location.assign('login.html');
            }
        }
    });
}
getUserInfo();

// 获取好友列表
function getFriendList() {
    $.ajax({
        type: 'get',
        url: 'friendList',
        success: function (body) {
            let friendListUL = document.querySelector('#friend-list');
            friendListUL.innerHTML = '';
            for (let friend of body) {
                let li = document.createElement('li');
                li.innerHTML = '<h4>' + friend.friendName + '</h4>';
                li.setAttribute('friend-id', friend.friendId);
                friendListUL.appendChild(li);

                li.onclick = function () {
                    clickFriend(friend);
                }
            }
        },
        error: function () {
            console.log('获取好友列表失败!');
        }
    });
}
getFriendList();

// 获取会话列表
function getSessionList() {
    $.ajax({
        type: 'get',
        url: 'sessionList',
        success: function (body) {
            let sessionListUL = document.querySelector('#session-list');
            sessionListUL.innerHTML = '';
            for (let session of body) {
                if (session.lastMessage.length > 10) {
                    session.lastMessage = session.lastMessage.substring(0, 10) + '...';
                }

                let li = document.createElement('li');
                li.setAttribute('message-session-id', session.sessionId);
                li.innerHTML = '<h3>' + session.friends[0].friendName + '</h3>'
                    + '<p>' + session.lastMessage + '</p>';
                sessionListUL.appendChild(li);

                li.onclick = function () {
                    clickSession(li);
                }
            }
        }
    });
}
getSessionList();

// 会话点击事件
function clickSession(currentLi) {
    let allLis = document.querySelectorAll('#session-list>li');
    activeSession(allLis, currentLi);
    let sessionId = currentLi.getAttribute("message-session-id");
    getHistoryMessage(sessionId);
}

// 取消未选中的会话的属性
function activeSession(allLis, currentLi) {
    for (let li of allLis) {
        if (li == currentLi) {
            li.className = 'selected';
        } else {
            li.className = '';
        }
    }
}

// 获取指定会话的所有历史消息
function getHistoryMessage(sessionId) {
    console.log("获取历史消息 sessionId=" + sessionId);
    let titleDiv = document.querySelector('.right .title');
    titleDiv.innerHTML = '';
    let messageShowDiv = document.querySelector('.right .message-show');
    messageShowDiv.innerHTML = '';

    let selectedH3 = document.querySelector('#session-list .selected>h3');
    if (selectedH3) {
        titleDiv.innerHTML = selectedH3.innerHTML;
    }

    $.ajax({
        type: 'get',
        url: 'message?sessionId=' + sessionId,
        success: function (body) {
            for (let message of body) {
                addMessage(messageShowDiv, message);
            }
            // 加控制滚动条自动滚动到最下方.
            scrollBottom(messageShowDiv);
        }
    });
}

// 会话添加一条消息
function addMessage(messageShowDiv, message) {
    let messageDiv = document.createElement('div');
    // 此处需要针对当前消息是不是用户自己发的, 决定是靠左还是靠右. 
    let selfUsername = document.querySelector('.left .user').innerHTML;
    if (selfUsername == message.fromName) {
        messageDiv.className = 'message message-right';
    } else {
        messageDiv.className = 'message message-left';
    }
    messageDiv.innerHTML = '<div class="box">'
        + '<h4>' + message.fromName + '</h4>'
        + '<p>' + message.content + '</p>'
        + '</div>';
    messageShowDiv.appendChild(messageDiv);
}

// 滚动条滚动到底部.
function scrollBottom(elem) {
    let clientHeight = elem.offsetHeight;
    let scrollHeight = elem.scrollHeight;
    elem.scrollTo(0, scrollHeight - clientHeight);
}

// 点击好友列表项, 触发的函数
function clickFriend(friend) {
    let sessionLi = findSessionByName(friend.friendName);
    let sessionListUL = document.querySelector('#session-list');
    if (sessionLi) {
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        sessionLi.click();
    } else {
        // 如果不存在匹配的结果, 就创建个新会话
        sessionLi = document.createElement('li');
        sessionLi.innerHTML = '<h3>' + friend.friendName + '</h3>' + '<p></p>';
        // 置顶
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        sessionLi.onclick = function () {
            clickSession(sessionLi);
        }
        sessionLi.click();
        createSession(friend.friendId, sessionLi);
    }
    // 标签页给切换到会话列表.
    let tabSession = document.querySelector('.tab .tab-session');
    tabSession.click();
}

// 在会话列表中寻找指定的会话
function findSessionByName(username) {
    let sessionLis = document.querySelectorAll('#session-list>li');
    for (let sessionLi of sessionLis) {
        let h3 = sessionLi.querySelector('h3');
        if (h3.innerHTML == username) {
            return sessionLi;
        }
    }
    return null;
}

// 创建会话
function createSession(friendId, sessionLi) {
    $.ajax({
        type: 'post',
        url: 'session?toUserId=' + friendId,
        success: function (body) {
            console.log("会话创建成功! sessionId = " + body.sessionId);
            sessionLi.setAttribute('message-session-id', body.sessionId);
        },
        error: function () {
            console.log('会话创建失败!');
        }
    });
}