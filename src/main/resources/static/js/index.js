$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//获取标题内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//发送异步请求 post
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{"title":title,"content":content},
		function (data) {
			data = $.parseJSON(data);
			//在提示框中返回消息
			$("#hintBody").text(data.msg);
			//显示提示框，2秒后隐藏
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if(data.code===0){
					window.location.reload();
				}
			}, 2000);
		}
		);



}