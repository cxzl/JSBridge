;(function() {
	if (window.WebViewJavascriptBridge) {
		return;
	}

	window.WebViewJavascriptBridge = {
		registerWebInterface: registerWebInterface,
		callClient: callClient,
		clientFetchQueue: clientFetchQueue,
		clientFetchResponseQueue: clientFetchResponseQueue,
		clientSendMessageToWeb: clientSendMessageToWeb
	};

	var CUSTOM_PROTOCOL_SCHEME = 'cxzlscheme';
	var QUEUE_HAS_MESSAGE = '://QUEUE_MESSAGE';
	var QUEUE_RESPONSE_MESSAGE = '://QUEUE_RESPONSE_MESSAGE';
	var CALLBACK_ID = "web_callback_";
	var messagingIframe;
	var sendMessageQueue = [];
	var responseMessageQueue = [];
	var webInterfaces = {};
	var responseCallbacks = {};
	var uniqueId = 1;

	//供客户端调用 客户端发送消息
	function clientSendMessageToWeb(messageJSON){
		console.log("WebViewJavascriptBridge: Info: " + messageJSON);
		_dispatchMessageFromClient(messageJSON);
	}

	//供客户端调用 客户端获取消息
	function clientFetchQueue() {
		var messageQueueString = JSON.stringify(sendMessageQueue);
		sendMessageQueue = [];
		console.log("WebViewJavascriptBridge: Info: " + messageQueueString);
		return messageQueueString;
	}

	//供客户端调用 客户端获取回调
	function clientFetchResponseQueue() {
		var responseQueueString = JSON.stringify(responseMessageQueue);
		responseMessageQueue = [];
		console.log("WebViewJavascriptBridge: Info: " + responseQueueString);
		return responseQueueString;
	}

	//注册jsbridge接口
	function registerWebInterface(interfaceName, interface) {
		webInterfaces[interfaceName] = interface;
	}

	//web端请求客户端接口
	function callClient(interfaceName, data, responseCallback) {
		if (arguments.length == 2 && typeof data == 'function') {
			responseCallback = data;
			data = null;
		}
		_doSend({
			interfaceName:interfaceName, data:data
		}, responseCallback);
	}

	//web端发送消息给客户端
	function _doSend(message, responseCallback) {
		//有回调的给消息加上callbackId
		if (responseCallback) {
			var callbackId = CALLBACK_ID + (uniqueId++) + '_' + new Date().getTime();
			responseCallbacks[callbackId] = responseCallback;
			message['callbackId'] = callbackId;
		}
		//消息入队
		sendMessageQueue.push(message);
		//通知客户端同步消息队列
		messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + QUEUE_HAS_MESSAGE;
	}

	//web端回复消息给客户端
	function _doResponse(message) {
		//消息入队
		responseMessageQueue.push(message);
		//通知客户端同步消息队列
		messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + QUEUE_RESPONSE_MESSAGE;
	}

	//处理来自客户端的消息
	function _dispatchMessageFromClient(messageJSON) {
		setTimeout(function () {
			try{
				var message = JSON.parse(messageJSON);
			}catch (exception) {
				console.log("WebViewJavascriptBridge: WARNING: javascript parse threw.", message, exception);
			}
			var responseCallback;
			//如果有responseId，说明是客户端回复的消息
			if (message.responseId) {
				responseCallback = responseCallbacks[message.responseId];
				if (!responseCallback) {
					return;
				}
				responseCallback(message.responseData);
				delete responseCallbacks[message.responseId];
			//else代表是客户端请求的消息
			} else {
				//客户端需要callback
				if (message.callbackId) {
					var callbackResponseId = message.callbackId;
					responseCallback = function(responseCode,responseData) {
						_doResponse({
							responseId:callbackResponseId,
							responseCode:responseCode,
							responseData:responseData
						});
					};
				}
				//消息交给具体的接口处理
				var webInterface = webInterfaces[message.interfaceName];
				try {
					webInterface(message.data, responseCallback);
				} catch(exception) {
					responseCallback(-1,exception);
					console.log("WebViewJavascriptBridge: WARNING: javascript webInterface threw.", message, exception);
				}
				if(!webInterface){
				    responseCallback(-1,"no webInterface for message from Java");
					console.log("WebViewJavascriptBridge: WARNING: no webInterface for message from Java:", message);
				}
			}
		});
	}

	var doc = document;
	//给客户端发送消息的无界面窗口
	messagingIframe = doc.createElement('iframe');
	messagingIframe.style.display = 'none';
	doc.documentElement.appendChild(messagingIframe);
	//通知jsbridge加载完毕
	var readyEvent = doc.createEvent('Events');
	readyEvent.initEvent('WebViewJavascriptBridgeReady');
	readyEvent.bridge = WebViewJavascriptBridge;
	doc.dispatchEvent(readyEvent);
})();