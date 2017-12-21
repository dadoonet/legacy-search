'use strict';

// Register `advanced` component, along with its associated controller and template
angular.
  module('advanced').
  component('advanced', {
    templateUrl: 'advanced/advanced.template.html',
    controller: ['$http', function AdvancedController($http) {
      var self = this;
      self.name = "";
      self.country = "";
      self.city = "";

      self.advanced_search = function() {
        $http({method: 'GET', url: '/api/1/person/_advanced_search?from=0&size=10&country='+self.country+'&city='+self.city+'&name='+ self.name })
            .then(function successCallback(response) {
              self.error = null;
              self.result = response.data;
              console.log(self.result.hits.total);
            }, function errorCallback(response) {
                self.error = "Backend not available";
            });
      };

      self.advanced_search();
    }]
  });
