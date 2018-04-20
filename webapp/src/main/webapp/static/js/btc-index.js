function ajaxBefore() {
	$('#show-message-container').append('<div class="message-loading-overlay"><i class="icon-spin icon-spinner orange2 bigger-160"></i></div>');
	setTimeout(function(){
		//$('#show-message-container').find('.message-loading-overlay').remove();
		var messageloading = $('#show-message-container').find('.message-loading-overlay');
		messageloading.remove();
		if(messageloading.length>0)
		    btcMessage("后台提示",'任务已经运行30秒,尚未返回结果，如果需要查看任务结果，请在此页面等待，您也可以前往  <a href="/manager/listBackTask" class="orange">后台任务</a> 中查看任务执行进度，或者做其他的操作');
	},30000);
}

function ajaxSuccess(responseText,textStatus) {
	$('#show-message-container').find('.message-loading-overlay').remove();
}

function ajaxError(XMLHttpRequest, error, thrownError) {
	$('#show-message-container').find('.message-loading-overlay').remove();
	var text = XMLHttpRequest.responseText;
	var status=XMLHttpRequest.status;
	btcMessage("异常信息"+status,text); 
}


function callback(responseText, textStatus, XMLHttpRequest) {
	 
}

var ajaxOptions = {
		targetId : null,
		url:null,
		params : null,
		before : ajaxBefore,
		success : ajaxSuccess,
		callback: callback
};

function simpleAjax(ajaxOptions) {
	$.ajax( {
		type : "POST",
		url : ajaxOptions.url,
		dataType : "text",
		data : ajaxOptions.params,
		beforeSend : ajaxOptions.before,
		success : ajaxOptions.success,
		complete:ajaxOptions.callback,
		error : ajaxError
	});
}


function btcConfirm(msg,confirmCallback){
	 bootbox.dialog({  
	        buttons: {  
	            confirm: {  
	                label: '确认',  
	                callback:confirmCallback,
	                className: 'btn-success'  
	            },  
	            cancel: {  
	                label: '取消',  
	                callback:emptFunction,
	                className: 'btn-default'  
	            }  
	        },  
	        message: msg,  
	        });
	
}

// 增加取现callback
function btcConfirm(msg,confirmCallback, cancelCallback){
	 bootbox.dialog({  
	        buttons: {  
	            confirm: {  
	                label: '确认',  
	                callback:confirmCallback,
	                className: 'btn-success'  
	            },  
	            cancel: {  
	                label: '取消',  
	                callback:cancelCallback,
	                className: 'btn-default'  
	            }  
	        },  
	        message: msg,  
	        });
	
}

function redirectTo(url){
	window.location.href=url;
}


function btcAlert(msg){
	bootbox.dialog({
		message: "<span class='bigger-110'>"+msg+"</span>",
		buttons: 			
		{
			"确定" :
			 {
				"label" : "<i class='icon-ok'></i> 确定",
				"className" : "btn-sm btn-success",
				"callback": emptFunction
			}
		}
	});
}

function btcAlert(msg,alertCallback){
	bootbox.dialog({
		message: "<span class='bigger-110'>"+msg+"</span>",
		buttons: 			
		{
			"确定" :
			 {
				"label" : "<i class='icon-ok'></i> 确定",
				"className" : "btn-sm btn-success",
				"callback": alertCallback
			}
		}
	});
}


function emptFunction(){}

function btcMessage(title,msg){
	$.gritter.add({
		title: title,
		text: msg,
		time:3000
   }); 
}


Date.prototype.format = function(format) {
    var o = {
    "M+" : this.getMonth() + 1, //month
    "d+" : this.getDate(), //day
    "h+" : this.getHours(), //hour
    "m+" : this.getMinutes(), //minute
    "s+" : this.getSeconds(), //second
    "q+" : Math.floor((this.getMonth() + 3) / 3), //quarter
    "S" : this.getMilliseconds()
    }
    if (/(y+)/.test(format)) {
        format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
 
    for ( var k in o) {
        if (new RegExp("(" + k + ")").test(format)) {
        format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
        }
    }
    return format;
};


function isNUMber(str){
	 if(/^\d+$/.test(str))    
	    {    
	        return true;   
	    } 
		return false;
	 }
