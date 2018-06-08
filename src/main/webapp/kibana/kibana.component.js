'use strict';

// Register `kibana` component, along with its associated controller and template
angular.
  module('kibana').
  component('kibana', {
    templateUrl: 'kibana/kibana.template.html',
    controller: ['$http', function KibanaController($http) {
    }]
  });
