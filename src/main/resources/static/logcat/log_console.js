/**
 * 处理控制台日志打印按钮操作
 */
$(function () {
    // 禁止滚动条
    $(document.body).css({
        "overflow-x": "hidden",
        "overflow-y": "hidden"
    });
    $(document).ready(function () {
        openSocket()
    });
    reqeustProject();
    var curProject = sessionStorage.getItem("cur_project");
    platformGroup(curProject);
    var keyword = sessionStorage.getItem("keyword");
    $("#keywordFilter").val(keyword);
    var platform = sessionStorage.getItem("platform");
    $("#platform").val(platform);
    var level = sessionStorage.getItem("level");
    if (level == null) {
        level = 0;
    }
    $("#cur_system").val(level);
    $("#cur_system").change(function () {
        var system = $("#cur_system").val();
        removeAllMessage(system, "level");
        sessionStorage.setItem("level", system);
    });
    $("#keywordFilter").on('input propertychange', function () {
        var keyword = $("#keywordFilter").val();
        removeAllMessage(keyword, "keyword");
        sessionStorage.setItem("keyword", keyword);
        $("#log_console").empty();
    });
    $("#openLog").click(function () {
        openSocket();
    });

    $("#closeLog").click(function () {
        mouseHover = false;
        closeWebSocket();
    });
    $("#clearLog").click(function () {
        mouseHover = false;
        $("#log_console").empty();
    });
    //关键词过滤
    $("#keywordFilter").change(function () {
        var keyword = $("#keywordFilter").val();
        removeAllMessage(keyword, "keyword");
        sessionStorage.setItem("keyword", keyword);
    });
    //系统过滤
    $("#cur_system").change(function () {
        var system = $("#cur_system").val();
        removeAllMessage(system, "cur_system");
        sessionStorage.setItem("cur_system", system);
        platformGroup();
    });
    platformGroup();
    //平台过滤
    $("#platform").change(function () {
        var platform = $("#platform").val();
        removeAllMessage(platform, "platform");
        sessionStorage.setItem("platform", platform);
    });
    $("#cur_project").change(function () {
        var project = $("#cur_project").val();
        if ("All" == project) {
            project = "";
        }
        removeAllMessage(project, "cur_project");
        sessionStorage.setItem("cur_project", project);
        platformGroup();
    });
    $("#st-trigger-effects1 >img").click(function () {
        var container = document.getElementById('st-container');
        classie.remove(container, 'st-menu-open');
    });
    $('div.logContent').mousewheel(function (event, delta, deltaX, deltaY) {
        //监听鼠标滚轮，向上滚动，则停止自动滚动，向下滚动则自动滚动到底部
        if (delta <= 0) {
            var nScrollHeight = $(this)[0].scrollHeight;
            var nScrollTop = $(this)[0].scrollTop;
            var nDivHeight = Math.round($("#log_console").height());
            var paddingBottom = parseInt($(this).css('padding-bottom')),
                paddingTop = parseInt($(this).css('padding-top'));
            var scrollToBottom = (nScrollTop + nDivHeight + paddingBottom + paddingTop) >= nScrollHeight;
            mouseHover = !scrollToBottom;
            postShowMsg();
        } else {
            mouseHover = delta > 0;
        }
    });
    // $("#log_console").scroll(function () {
    //     var nScrollHeight = $(this)[0].scrollHeight;
    //     var nScrollTop = $(this)[0].scrollTop;
    //     var nDivHeight = Math.round($("#log_console").height());
    //     var paddingBottom = parseInt($(this).css('padding-bottom')), paddingTop = parseInt($(this).css('padding-top'));
    //     var scrollToBottom = (nScrollTop + nDivHeight + paddingBottom + paddingTop) >= nScrollHeight;
    //     mouseHover = !scrollToBottom;
    //     // console.log("divScroll>>>" + (scrollToBottom ? "滚动条到底部了" : "没有滚动底部")
    //     //     + "\nmouseHover:" + mouseHover + ",nDivHeight:" + nDivHeight
    //     //     + ",\nnScrollTop:" + nScrollTop + ",nScrollHeight:" + nScrollHeight
    //     //     + ",paddingBottom:" + paddingBottom + ",paddingTop:" + paddingTop);
    // });

});

/**
 * 根据条件删除缓存的历史记录
 * @param changeValue
 * @param valueName
 * @author dingpeihua
 * @date 2019/10/22 14:32
 * @version 1.0
 */
function removeAllMessage(changeValue, valueName) {
    var oldValue = sessionStorage.getItem(valueName);
    if (changeValue !== oldValue) {
        divMessages.splice(0, divMessages.length);
    }
}

var divMessages = [];
var allMsgNumber = 0;
var websocket = null;
var maxLogCount = 30;
var lastHeartBeat;
var mouseHover = false;

//关闭连接
function closeWebSocket() {
    websocket.close();
}

/**
 * 系统消息
 * @type {number}
 */
var SYSTEM_MSG_CODE = 0;
/**
 * 用户消息
 * @type {number}
 */
var USER_MSG_CODE = 1;

function openSocket() {
    mouseHover = false;
    if (websocket != null) {
        closeWebSocket()
    }
    lastHeartBeat = new Date().getTime();
    //判断当前浏览器是否支持WebSocket
    let platform = sessionStorage.getItem("platform_name"); //获取
    let curSchema = window.location.protocol;
    let socketSchema = "ws://";
    if (curSchema.indexOf("https") >= 0) {
        socketSchema = "wss://";
    }
    let socketUrl = socketSchema + window.location.host + "/websocket?platform_name=" + platform;
    if ('WebSocket' in window) {
        websocket = new WebSocket(socketUrl);
    } else if ('MozWebSocket' in window) {
        websocket = new MozMozWebSocket(socketUrl);
    } else {
        alert("你的浏览器不支持");
        return;
    }
    websocket.onerror = function () {
        postMessage(JSON.stringify({code: SYSTEM_MSG_CODE, msg: "连接错误，请重试..."}));
    };

    //连接成功建立的回调方法
    websocket.onopen = function (event) {
        postMessage(JSON.stringify({code: SYSTEM_MSG_CODE, msg: "连接成功了..."}));
    };
    //接收到消息的回调方法
    websocket.onmessage = function (event) {
        lastHeartBeat = new Date().getTime();
        postMessage(event.data);
    };
    //连接关闭的回调方法
    websocket.onclose = function () {
        postMessage(JSON.stringify({code: SYSTEM_MSG_CODE, msg: "连接关闭了..."}));
    };
    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        websocket.close();
    }
}

const worker = new Worker("static/pc/logcat/task.js");
worker.onmessage = function (message) {
    if (divMessages.length >= maxLogCount) {
        divMessages.shift();
    }
    divMessages.push(message.data);
    postShowMsg();
};

worker.onerror = function (error) {
    console.log(error.filename, error.lineno, error.message);
};

function postShowMsg() {
    if (divMessages.length > 0 && !mouseHover) {
        showMessage(divMessages.shift());
        console.log('messages.length :' + divMessages.length);
        if (divMessages.length > 0 && !mouseHover) {
            setTimeout(postShowMsg, 200);
        }
    }
}

function postMessage(message) {
    const messages = {};
    messages.curSystem = sessionStorage.getItem("cur_system");
    messages.data = message;
    messages.keyword = sessionStorage.getItem("keyword");
    messages.platform = sessionStorage.getItem("platform");
    messages.curProject = sessionStorage.getItem("cur_project");
    worker.postMessage(messages);
    allMsgNumber = allMsgNumber + 1;
}

//将消息显示在网页上
function showMessage(data) {
    var div = document.getElementById('log_console');
    var length = div.childElementCount;
    if (length >= maxLogCount) {
        var childD = div.childNodes.item(length - (maxLogCount - 1));
        $(childD).prevAll().remove();
    }
    var childDiv = document.createElement("div");
    var content = data;
    try {
        //解码url
        content = decodeURIComponent(data)
    } catch (e) {
        console.log("error:" + e.message)
    }
    childDiv.innerHTML = content;
    div.appendChild(childDiv);
    div.scrollTop = div.scrollHeight;//这里是关键的实现
}

/**
 * 移除第一个
 * @author dingpeihua
 * @date 2019/10/22 11:54
 * @version 1.0
 */
function removeLog(div) {
    if (logCount >= maxLogCount) {
        div.children().first().remove();
    }
}

function platformGroup() {
    let project = sessionStorage.getItem("cur_project");
    const system = sessionStorage.getItem("cur_system");
    console.log("system>>" + system + ",project>>" + project);
    let platform = "Android";
    if (system === '1') {
        platform = "Android";
    } else if (system === '2') {
        platform = "iOS";
    }
    $.ajax({
        type: "POST",
        url: "/log/platformGroup",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify({
            appName: project,
            platform: platform
        }),
        dataType: "json",
        success: function (message) {
            refreshPlatform(message);
        },
        error: function (message) {
        }
    });
}

function reqeustProject() {
    $.ajax({
        type: "POST",
        url: "/log/projectGroup",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (message) {
            refreshProjects(message);
        },
        error: function (message) {
        }
    });
}

function refreshPlatform(message) {
    var mPlatform = sessionStorage.getItem("platform"); //获取
    var selected = false;
    $("#platform").empty();
    $("#platform").append("<option value=\"\">平台过滤...</option>");
    for (var i = 0; i < message.length; i++) {
        var platform = message[i];
        if (platform != null) {
            if (mPlatform == platform) {
                $("#platform").append("<option value='" + platform + "' selected='true'>" + platform + "</option>");
                selected = true;
            } else {
                $("#platform").append("<option value='" + platform + "'>" + platform + "</option>");
            }
        }
    }
    //选择已清空
    if (!selected) {
        sessionStorage.removeItem("platform")
    }
}

function refreshProjects(projects) {
    var curProject = sessionStorage.getItem("cur_project"); //获取
    var selected = false;
    $("#cur_project").empty();
    $("#cur_project").append("<option value=\"\">项目过滤...</option>");
    for (var i = 0; i < projects.length; i++) {
        var project = projects[i];
        if (project != null) {
            if (curProject == project) {
                $("#cur_project").append("<option value='" + project + "' selected='true'>" + project + "</option>");
                selected = true;
            } else {
                $("#cur_project").append("<option value='" + project + "'>" + project + "</option>");
            }
        }
    }
    //选择已清空
    if (!selected) {
        sessionStorage.removeItem("cur_project")
    }
}

function isNull(data) {
    return (data == "" || data == undefined || data == null);
}
