'use strict';

angular.
  module('legacyApp').
  constant("config", {
//    "backend": "http://0.0.0.0:8080/" // For local tests
    "backend": ""
  }).
  config(['$locationProvider' ,'$routeProvider',
    function config($locationProvider, $routeProvider) {
      $locationProvider.hashPrefix('!');

      $routeProvider.
        when('/search', {template: '<search></search>'}).
        when('/init', {template: '<init></init>' }).
        when('/compute', {template: '<compute></compute>' }).
        when('/advanced', {template: '<advanced></advanced>' }).
        when('/kibana', {template: '<kibana></kibana>' }).
        when('/person/:id', {template: '<person-detail></person-detail>'}).
        otherwise({redirectTo: '/search'});

      console.log("Project running with AngularJS " + angular.version.full);
    }
  ]);
