$(function(){
  $.ajax({
    url:'/memo/memo_list',
    success:function(data){
      for(var i = 0; i < data.length; i++) {
        $html_string = '<tr>';
        $html_string = $html_string + '<td>' + data[i]['idx'] + '</td>';
        $html_string = $html_string + '<td>' + data[i]['text'] + '</td>';
        $html_string = $html_string + '</tr>';
        $('table').append($html_string);
      }
      //  console.log(data);
    }
  });
});



