var oEditors = [];
$(function(){
  $.ajax({
    url:'/memo/posts_details',
    data:{'idx': idx},
    success:function(data){
      $('text').text(data['text']);
      nhn.husky.EZCreator.createInIFrame({
       oAppRef: oEditors,
       elPlaceHolder: "text",
       sSkinURI: "/memo/resources/smarteditor/SmartEditor2Skin.html",
       fCreator: "createSEditor2"
      });
    }
  });
});

$(document).on('click', '.submit_button', function(event) {
	oEditors.getById["text"].exec("UPDATE_CONTENTS_FIELD", []);
	var idx = $('#idx').val();
	var text = $('#text').val();
	$.ajax({
		url : '/memo/posts_mod',
		data : {
			'idx' : idx,
			'text' : text
		},
		success : function(data) {
			history.back();
			// console.log(data);
		}
	});

});
