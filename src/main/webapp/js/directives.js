'use strict';

/* Directives */


angular.module('myApp.directives', [])
	.directive('appVersion', ['version', function(version) {
    	return function(scope, elm, attrs) {elm.text(version);};
  	}])
    .directive('zKeypress', function(){
        return {
            restrict: 'A',
            link: function(scope, elem, attr, ctrl) {
                elem.bind('keyup', function(){
                    scope.$apply(function(s) {
                        s.$eval(attr.zKeypress);
                    });
                });
            }
        };
	});


