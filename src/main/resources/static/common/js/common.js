var commonLib = commonLib ?? {};

/*
    메타 태그 정보 조회
    mode - rootUrl : <meta name="rootUrl" ... />
*/
commonLib.getMeta = function(mode) {
    if (!mode) return;

    const el = document.querySelector(`meta[name='${mode}']`);

    return el?.content; // 옵셔널 체이닝(optional chaining?)
};

window.addEventListener("DOMContentLoaded", function() {
    // 체크박스 전체 토글 기능 S
    const checkAlls = document.getElementsByClassName("check-all");
    for (const el of checkAlls) {
        el.addEventListener("click", function() {
            const { targetClass } = this.dataset;
            if (!targetClass) { // 토클할 체크박스의 클래스가 설정되지 않은 경우는 진행 X
                return;
            }

            const chks = document.getElementsByClassName(targetClass);
            for (const chk of chks) {
                chk.checked = this.checked;
            }
        });
    }
    // 체크박스 전체 토글 기능 E
});
