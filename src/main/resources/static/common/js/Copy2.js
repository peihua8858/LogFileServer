$('.clipboard').tooltip({
    trigger: 'click',
    placement: 'bottom'
});

function setTooltip(btn, message) {
    $(btn).tooltip('hide')
        .attr('data-original-title', message)
        .tooltip('show');
}

function hideTooltip(btn) {
    setTimeout(function () {
        $(btn).tooltip('hide');
    }, 1000);
}

const clipboard = new ClipboardJS('.clipboard', {
    text: function (trigger) {
        let funcName = trigger.dataset.call;
        let func = eval(funcName);
        return func();
    }
});

clipboard.on('success', function (e) {
    setTooltip(e.trigger, '拷贝成功！');
    hideTooltip(e.trigger);
});

clipboard.on('error', function (e) {
    setTooltip(e.trigger, '拷贝失败!');
    hideTooltip(e.trigger);
});