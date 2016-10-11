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
            .success(function(data, status, headers, config) {
              console.log(data.hits.total);
              self.result = data;
            })
            .error(function(data, status, headers, config) {
              self.log = 'Error!';
            });
      };

      self.advanced_search();
    }]
  });
