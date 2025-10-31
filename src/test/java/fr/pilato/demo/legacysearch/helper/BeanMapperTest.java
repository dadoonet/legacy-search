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
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for BeanMapper utility class.
 */
public class BeanMapperTest {

    @Test
    public void testMapSimpleProperties() {
        Person source = new Person();
        source.setName("John Doe");
        source.setGender("male");
        source.setChildren(2);
        source.setDateOfBirth(LocalDate.of(1990, 1, 1));

        Person target = new Person();
        target.setName("Jane Doe");
        target.setGender("female");

        BeanMapper.map(source, target);

        assertEquals("John Doe", target.getName());
        assertEquals("male", target.getGender());
        assertEquals(2, target.getChildren());
        assertEquals(LocalDate.of(1990, 1, 1), target.getDateOfBirth());
    }

    @Test
    public void testMapDoesNotOverrideWithNull() {
        Person source = new Person();
        source.setName("John Doe");
        // gender is null

        Person target = new Person();
        target.setName("Jane Doe");
        target.setGender("female");

        BeanMapper.map(source, target);

        assertEquals("John Doe", target.getName());
        assertEquals("female", target.getGender()); // Should not be overridden with null
    }

    @Test
    public void testMapNestedAddress() {
        Person source = new Person();
        Address sourceAddress = new Address();
        sourceAddress.setCountry("France");
        sourceAddress.setCity("Paris");
        sourceAddress.setZipcode("75001");
        sourceAddress.setCountrycode("FR");
        sourceAddress.setLocation(new GeoPoint(48.84, 2.31));
        source.setAddress(sourceAddress);

        Person target = new Person();
        Address targetAddress = new Address();
        targetAddress.setCountry("USA");
        targetAddress.setCity("New York");
        target.setAddress(targetAddress);

        BeanMapper.map(source, target);

        assertNotNull(target.getAddress());
        assertEquals("France", target.getAddress().getCountry());
        assertEquals("Paris", target.getAddress().getCity());
        assertEquals("75001", target.getAddress().getZipcode());
        assertEquals("FR", target.getAddress().getCountrycode());
        assertNotNull(target.getAddress().getLocation());
        assertEquals(48.84, target.getAddress().getLocation().getLat());
        assertEquals(2.31, target.getAddress().getLocation().getLon());
    }

    @Test
    public void testMapCreatesNestedAddressIfNull() {
        Person source = new Person();
        Address sourceAddress = new Address();
        sourceAddress.setCountry("France");
        sourceAddress.setCity("Paris");
        source.setAddress(sourceAddress);

        Person target = new Person();
        // target address is null

        BeanMapper.map(source, target);

        assertNotNull(target.getAddress());
        assertEquals("France", target.getAddress().getCountry());
        assertEquals("Paris", target.getAddress().getCity());
    }

    @Test
    public void testMapNestedMarketing() {
        Person source = new Person();
        Marketing sourceMarketing = new Marketing();
        sourceMarketing.setCars(5);
        sourceMarketing.setShoes(10);
        sourceMarketing.setFood(3);
        source.setMarketing(sourceMarketing);

        Person target = new Person();
        Marketing targetMarketing = new Marketing();
        targetMarketing.setCars(1);
        target.setMarketing(targetMarketing);

        BeanMapper.map(source, target);

        assertNotNull(target.getMarketing());
        assertEquals(5, target.getMarketing().getCars());
        assertEquals(10, target.getMarketing().getShoes());
        assertEquals(3, target.getMarketing().getFood());
    }

    @Test
    public void testMapCreatesNestedMarketingIfNull() {
        Person source = new Person();
        Marketing sourceMarketing = new Marketing();
        sourceMarketing.setCars(5);
        source.setMarketing(sourceMarketing);

        Person target = new Person();
        // target marketing is null

        BeanMapper.map(source, target);

        assertNotNull(target.getMarketing());
        assertEquals(5, target.getMarketing().getCars());
    }

    @Test
    public void testMapHandlesNullSource() {
        Person target = new Person();
        target.setName("Jane Doe");

        BeanMapper.map(null, target);

        assertEquals("Jane Doe", target.getName()); // Should remain unchanged
    }

    @Test
    public void testMapHandlesNullTarget() {
        Person source = new Person();
        source.setName("John Doe");

        // Should not throw exception
        BeanMapper.map(source, null);
    }
}
