'use strict';

// Register `init` component, along with its associated controller and template
angular.
  module('init').
  component('init', {
    templateUrl: 'init/init.template.html',
    controller: ['$http', '$interval', 'config', function InitController($http, $interval, config) {
      var self = this;
      self.persons = "";
      self.status = "active";
      self.progress = {
        took: 0,
        rate: 0,
        current: 0
      };
      self.remaining = 0;
      self.goal = 0;
      self.took = 0;
      self.result = null;

      var stop;
      self.startWatch = function() {
        if ( angular.isDefined(stop) ) return;

        stop = $interval(function() {
          // Poll the status API
          $http({method: 'GET', url: config.backend + '/api/1/person/_init_status' })
              .then(function successCallback(response) {
                self.progress = response.data;
                // Remaining docs
                var remaining_docs = self.persons - self.progress.current;
                self.remaining = Math.round(remaining_docs / self.progress.rate);
                self.took = Math.round(self.progress.took / 1000);
              });
        }, 100);
      };

      self.stopWatch = function() {
        if (angular.isDefined(stop)) {
          $interval.cancel(stop);
          stop = undefined;
        }
      };

      self.init = function() {
        self.status = "active";
        self.result = null;
        self.progress = {
          took: 0,
          rate: 0,
          current: 0
        };
        self.remaining = 0;
        self.goal = self.persons;
        self.took = 0;
        self.startWatch();
        $http({method: 'GET', url: config.backend + '/api/1/person/_init?size='+self.persons })
            .then(function successCallback(response) {
              self.result = response.data;
              self.progress = self.result;
              self.status = "progress-bar-success";
              self.stopWatch();
        });
      }
    }]
  });
