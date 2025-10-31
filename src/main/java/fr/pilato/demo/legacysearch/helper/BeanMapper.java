/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.pilato.demo.legacysearch.helper;

import fr.pilato.demo.legacysearch.domain.Address;
import fr.pilato.demo.legacysearch.domain.GeoPoint;
import fr.pilato.demo.legacysearch.domain.Marketing;
import fr.pilato.demo.legacysearch.domain.Person;

/**
 * Utility class for copying non-null properties from one bean to another.
 * This replaces the Dozer library functionality.
 */
public class BeanMapper {

    /**
     * Maps properties from source Person to target Person.
     * Only non-null properties from source are copied to target.
     * 
     * @param source the source Person object
     * @param target the target Person object to update
     */
    public static void map(Person source, Person target) {
        if (source == null || target == null) {
            return;
        }

        // Copy simple properties
        if (source.getName() != null) {
            target.setName(source.getName());
        }
        if (source.getDateOfBirth() != null) {
            target.setDateOfBirth(source.getDateOfBirth());
        }
        if (source.getGender() != null) {
            target.setGender(source.getGender());
        }
        if (source.getChildren() != null) {
            target.setChildren(source.getChildren());
        }

        // Copy nested Marketing object
        if (source.getMarketing() != null) {
            if (target.getMarketing() == null) {
                target.setMarketing(new Marketing());
            }
            mapMarketing(source.getMarketing(), target.getMarketing());
        }

        // Copy nested Address object
        if (source.getAddress() != null) {
            if (target.getAddress() == null) {
                target.setAddress(new Address());
            }
            mapAddress(source.getAddress(), target.getAddress());
        }
    }

    /**
     * Maps properties from source Marketing to target Marketing.
     * 
     * @param source the source Marketing object
     * @param target the target Marketing object to update
     */
    private static void mapMarketing(Marketing source, Marketing target) {
        if (source.getCars() != null) {
            target.setCars(source.getCars());
        }
        if (source.getShoes() != null) {
            target.setShoes(source.getShoes());
        }
        if (source.getToys() != null) {
            target.setToys(source.getToys());
        }
        if (source.getFashion() != null) {
            target.setFashion(source.getFashion());
        }
        if (source.getMusic() != null) {
            target.setMusic(source.getMusic());
        }
        if (source.getGarden() != null) {
            target.setGarden(source.getGarden());
        }
        if (source.getElectronic() != null) {
            target.setElectronic(source.getElectronic());
        }
        if (source.getHifi() != null) {
            target.setHifi(source.getHifi());
        }
        if (source.getFood() != null) {
            target.setFood(source.getFood());
        }
    }

    /**
     * Maps properties from source Address to target Address.
     * 
     * @param source the source Address object
     * @param target the target Address object to update
     */
    private static void mapAddress(Address source, Address target) {
        if (source.getCountry() != null) {
            target.setCountry(source.getCountry());
        }
        if (source.getZipcode() != null) {
            target.setZipcode(source.getZipcode());
        }
        if (source.getCity() != null) {
            target.setCity(source.getCity());
        }
        if (source.getCountrycode() != null) {
            target.setCountrycode(source.getCountrycode());
        }
        if (source.getLocation() != null) {
            target.setLocation(source.getLocation());
        }
    }
}
