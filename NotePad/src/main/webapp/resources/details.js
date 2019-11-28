$(function(){
  $.ajax({
    url:'/memo/memo_list',
    success:function(data){
      for(var i = 0; i < data.length; i++) {
        $html_string = '<tr>';
        $html_string = $html_string + '<td>' + '<input type="checkbox" id="check" value="idx"/>' + '</td>';
        $html_string = $html_string + '<td>' + data[i]['idx'] + '</td>';
        $html_string = $html_string + '<td>' + data[i]['text'] + '</td>';
        $html_string = $html_string + '</tr>';
        $('table').append($html_string);
      }
      //  console.log(data);
    }
  });
});

//$(document).on('click','.update',function(){
//	location.href = "http://localhost:8888/memo/details2";
//});


$(document).on('click','.update',function(){
	var check = $('input:checkbox[id="check"]:checked').parents('tr').find('td').eq(1).text();
	location.href = "/memo/details2?idx="+check;
});