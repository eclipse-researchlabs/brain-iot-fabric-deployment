'use strict';

/* Controllers */

function SensorUICtl($scope, $http, $timeout) {

    $scope.message = "";
    $scope.messages = [];

    const getMessages = () => {
		$http.get("../messages").then(msgs => {
            console.log("messages", msgs.data);
            $scope.messages = msgs.data;
		});
    }


	$scope.trigger = () => {
		$http.post("../sensor", {message: $scope.message.trim()}).then(x => {
            getMessages();
		});
	    $scope.message = "";
	};

    getMessages();
}
