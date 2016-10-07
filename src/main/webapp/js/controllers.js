'use strict';

/* Controllers */


function AppCtrl($scope, $http) {
	$scope.user = null;
}
AppCtrl.$inject = ['$scope', '$http'];


function NavBarController($scope) {
}
NavBarController.$inject = ['$scope'];

function SearchCtrl($scope, $http) {
    $scope.query = "";
    $scope.f_date = "";
    $scope.f_country = "";

    $scope.search = function() {
        $http({method: 'GET', url: '/api/1/person/_search?from=0&size=10&q='+ $scope.query
            + '&f_date=' + $scope.f_date + '&f_country=' + $scope.f_country })
            .success(function(data, status, headers, config) {
                $scope.result = data;

                // Group data every 10 years (facets don't support it yet)
                $scope.dates = new Array();

                // If we have aggs, compute (for future use)
                if (data.aggregations) {
                    var buckets = data.aggregations.by_year.buckets;

                    var i = -1;
                    for (var idx in buckets) {
                        var year = buckets[idx].key_as_string;
                        var docs = buckets[idx].doc_count;
                        var subyear = year.substr(0,3);

                        if (i == -1 || subyear != $scope.dates[i].key) {
                            i++;
                            $scope.dates[i] = {};
                            $scope.dates[i].key = subyear;
                            $scope.dates[i].docs = docs;
                        } else {
                            $scope.dates[i].docs += docs;
                        }
                    }
                }
            })
            .error(function(data, status, headers, config) {
                $scope.name = 'Error!'
            });
    }

    $scope.addFilterCountry = function() {
        console.log(this.bucket.key);
        $scope.f_country = this.bucket.key;
        $scope.search();
    }

    $scope.addFilterDate = function() {
        console.log(this.bucket.key+"0");
        $scope.f_date = this.bucket.key+"0";
        $scope.search();
    }

    $scope.search();
}
SearchCtrl.$inject = ['$scope', '$http'];

function AdvancedSearchCtrl($scope, $http) {
    $scope.name = "";
    $scope.country = "";
    $scope.city = "";

    $scope.advanced_search = function() {

        $http({method: 'GET', url: '/api/1/person/_advanced_search?from=0&size=10&country='+$scope.country+'&city='+$scope.city+'&name='+ $scope.name }).success(function(data, status, headers, config) {
            $scope.result = data;
        })
            .error(function(data, status, headers, config) {
                $scope.log = 'Error!'
            });
    }

    $scope.advanced_search();
}
AdvancedSearchCtrl.$inject = ['$scope', '$http'];

function PersonFormCtrl($rootScope, $scope, $routeParams, $http, $location) {

    $http({method: 'GET', url: '/api/1/person/_byid/'+ $routeParams.id }).success(function(data, status, headers, config) {
        $scope.person = data;
    })
        .error(function(data, status, headers, config) {
            $scope.log = 'Error!'
        });


    $scope.save = function() {
        $http.put('/api/1/person/'+ $routeParams.id , $scope.person)
            .success(function(data, status, headers, config) { console.log( $scope.person ); })
            .error(function(data, status, headers, config) {
                $scope.name = 'Error!'
            });
    }



    $scope.delete = function() {
        $http({method: 'DELETE', url: '/api/1/person/'+ $routeParams.id }).success(function(data, status, headers, config) {
            $scope.person = data;
        })
            .error(function(data, status, headers, config) {
                $scope.name = 'Error!'
            });
        $location.path('/');
    }


}
PersonFormCtrl.$inject = ['$rootScope', '$scope', '$routeParams','$http', '$location'];

function InitCtrl($scope, $http) {
    $scope.persons = "";
    $scope.result = null;

    $scope.init = function() {
        $scope.result = false;
        $http({method: 'GET', url: '/api/1/person/_init?size='+$scope.persons }).success(function(data, status, headers, config) {
            $scope.result = true;
        })
            .error(function(data, status, headers, config) {
                $scope.log = 'Error!';
                $scope.error = true;
            });
    }
}
InitCtrl.$inject = ['$scope', '$http'];

