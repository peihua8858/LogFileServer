var tableList;
$(function () {
    var platform = sessionStorage.getItem("platform");
    console.log("platform>>>" + platform);
    if (!isNull(platform)) {
        $("#platform").append("<option value='" + platform + "' selected='true'>" + platform + "</option>");
    }
    //平台过滤
    $("#platform").change(function () {
        var platform = $("#platform").val();
        $('#table_list').dataTable().api().ajax.reload();
        sessionStorage.setItem("platform", platform);
        $("#platform").attr("display")
    });
    tableList = $(document).ready(function () {
        $('#table_list').dataTable({
            "bProcessing": true, //DataTables载入数据时，是否显示‘进度’提示
            "bServerSide": true, //是否启动服务器端数据导入
            "bPaginate": true, //是否显示（应用）分页器
            "sPaginationType": "simple_numbers", //详细分页组，可以支持直接跳转到某页
            "bSort": true, //是否启动各个字段的排序功能
            "bFilter": true, //是否启动过滤、搜索功能
            "serverSide": true,
            "order": [[4, "desc"]],
            responsive: true,
            lengthMenu: [ //自定义分页长度
                [20, 50, 100, 200],
                ['20/页', '50/页', '100/页', '200/页']
            ],
            "sAjaxSource": "/log/search",//这个是请求的地址
            "fnServerData": retrieveData,
            "columns": [
                {"data": "#", "orderable": false},
                {"data": "url"},
                {"data": "feeTime"},
                {"data": "updateTime"},
                {"data": "option", "orderable": false}
            ]
        });
    });
});

/**
 * 取回服务端返回的数据
 * @param url
 *
 *@param aoData
 * @param fnCallback
 * @author dingpeihua
 * @date 2019/6/21 16:00
 * @version 1.0
 */
function retrieveData(url, aoData, fnCallback) {
    const request = JSON.parse(JSON.stringify(aoData));
    let iDisplayStart = 0;
    let iDisplayLength = 20;
    let search = "";
    let iSortCol_0;
    let sSortDir_0;
    for (let i = 0; i < request.length; i++) {
        let objR = request[i];
        if (objR['name'] === "iDisplayStart") {
            iDisplayStart = objR["value"];
        }
        if (objR['name'] === "iDisplayLength") {
            iDisplayLength = objR["value"];
        }
        if (objR['name'] === "sSearch") {
            search = objR["value"];
        }
        if (objR['name'] === "iSortCol_0") {
            iSortCol_0 = objR["value"];
        }
        if (objR['name'] === "sSortDir_0") {
            sSortDir_0 = objR["value"];
        }
    }
    let curPage = (iDisplayStart / iDisplayLength);
    ++curPage;
    let pageSize = iDisplayLength;
    let platform = $("#platform").val();
    // var platform = sessionStorage.getItem("platform");
    $.ajax({
        url: url,//这个就是请求地址对应sAjaxSource
        data: JSON.stringify({
            search: search,
            platform: platform,
            current: curPage,
            pageSize: pageSize,
            orderByCol: iSortCol_0,
            orderBy: sSortDir_0
        }),
        type: 'POST',
        dataType: 'json',
        contentType: "application/json; charset=utf-8",
        async: true,
        success: function (result) {
            let resultObj = JSON.parse(JSON.stringify(result));
            let dataObj = resultObj.data;
            let array = [];
            let list = dataObj.result;
            $.each(list, function (index, data) {
                let id = data["id"].toString();
                let tempObj = {
                        "#": index,
                        "url": data["url"],
                        "feeTime": data["duration"] + "/" + data["visitsNumber"],
                        "updateTime": new Date(data["updateTime"]).Format("yyyy-MM-dd hh:mm:ss"),
                        "option": "<a href=\"/log/delete/" + id + "\" class=\"btn btn-light btn-sm\">删除</a>&nbsp;&nbsp;" +
                            "<a href=\"/log/show/" + id + "\"  target=\"_blank\"  class=\"btn btn-info btn-sm\">查看</a>&nbsp;&nbsp;" +
                            "<a href=\"/log/model/" + id + "\"  target=\"_blank\"  class=\"btn btn-info btn-sm\" data-toggle=\"tooltip\" " +
                            " data-placement=\"top\" title=\"快速模拟会帮你根据现在的接口内容生成模拟接口数据   \">一键建模</a>&nbsp;&nbsp;" +
                            "<button type=\"button\" onclick=\"lock('" + id + "','" + data["locking"] + "')\" " +
                            "                        class=\"" + (data["locking"] == 1 ? "btn btn-warning btn-secondary btn-sm" : "btn btn-success btn-sm") + "\"\n" +
                            "                        data-toggle=\"tooltip\" data-placement=\"top\"\n" +
                            "                        title=\"锁定状态，不会更新接口数据内容\">" + (data["locking"] == 1 ? "已锁定" : "未锁定") + "</button>"
                    }
                ;
                array.push(tempObj);
            });
            let returnData = {};
            returnData.recordsTotal = dataObj.totalNum;//返回数据全部记录
            returnData.recordsFiltered = dataObj.totalNum;//后台不实现过滤功能，每次查询均视作全部结果
            returnData.data = array;//返回的数据列表
            // console.log(returnData);
            refreshPlatform(dataObj.values);
            fnCallback(returnData);//把返回的数据传给这个方法就可以了,datatable会自动绑定数据的
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log("status:" + XMLHttpRequest.status + ",readyState:" + XMLHttpRequest.readyState + ",textStatus:" + textStatus);
        }
    });
}

function lock(id, lock) {
    lock = lock == 1 ? 0 : 1;
    $.ajax({
        type: "POST",
        url: "/log/lock",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify({id: id, locking: lock}),
        dataType: "json",
        success: function (message) {
            window.location.reload()
        },
        error: function (message) {
        }
    });
}

function clearLog() {
    var domain = $('#platform_name').val();
    var commd = prompt("请输入删除指令！", "");
    if (isNull(commd)) {
        return;
    }
    $.ajax({
        type: "POST",
        url: "/log/deleteAll",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify({domain: domain, command: commd}),
        dataType: "json",
        success: function (message) {
            var dataObj = JSON.parse(JSON.stringify(message));
            if (dataObj.code != 200) {
                alert(dataObj.msg);
            } else {
                window.location.reload();
            }
        },
        error: function (message) {
            var dataObj = JSON.parse(JSON.stringify(message));
            alert(dataObj.msg)
        }
    });
}

function search() {
    var domain = $('#platform_name').val().trim();
    var url = $('#platform_url').val().trim();
    window.location.href = '/log?domain=' + domain + '&url=' + url;
}

function refreshPlatform(message) {
    var curPlatform = sessionStorage.getItem("platform"); //获取
    var selected = false;
    $("#platform").empty();
    $("#platform").append("<option value=\"\">All</option>");
    for (var i = 0; i < message.length; i++) {
        var platform = message[i];
        if (platform != null) {
            if (curPlatform == platform) {
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

function filterEvent() {
    if (event.keyCode == 13) {
        search()
    }
}