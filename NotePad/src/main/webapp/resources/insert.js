var oEditors = [];
$(function() {
	nhn.husky.EZCreator.createInIFrame({
		oAppRef : oEditors,
		elPlaceHolder : "text",
		sSkinURI : "/memo/resources/smarteditor/SmartEditor2Skin.html",
		fCreator : "createSEditor2"
	});
});

$(document).on('click', '.submit_button', function(event) {
	oEditors.getById["text"].exec("UPDATE_CONTENTS_FIELD", []);

	var text = $('#text').val();
	$.ajax({
		url : '/memo/memo_insert',
		data : {
			'text' : text
		},
		success : function(data) {
			history.back();
			// console.log(data);
		}
	});

});
