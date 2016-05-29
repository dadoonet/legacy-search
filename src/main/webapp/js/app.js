'use strict';

// Declare app level module which depends on filters, and services
angular.module('myApp', ['myApp.filters', 'myApp.services', 'myApp.directives']).
  config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider.
        when('/', {templateUrl: 'partials/search.html', controller: SearchCtrl }).
        when('/init', {templateUrl: 'partials/init.html', controller: InitCtrl }).
        when('/compute', {templateUrl: 'partials/compute.html', controller: SearchCtrl }).
        when('/advanced', {templateUrl: 'partials/advanced.html', controller: AdvancedSearchCtrl }).
        when('/person/:id', {templateUrl: 'partials/person-form.html', controller: PersonFormCtrl }).
		otherwise({redirectTo: '/'});
  }]);


