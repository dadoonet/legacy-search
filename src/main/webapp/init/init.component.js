'use strict';

// Register `init` component, along with its associated controller and template
angular.
  module('init').
  component('init', {
    templateUrl: 'init/init.template.html',
    controller: ['$http', 'config', function InitController($http, config) {
      var self = this;
      self.persons = "";
      self.result = null;

      self.init = function() {
        self.result = false;
        $http({method: 'GET', url: config.backend + '/api/1/person/_init?size='+self.persons })
            .success(function(data, status, headers, config) {
              self.result = true;
              self.injected = self.persons;
        });
      }
    }]
  });
