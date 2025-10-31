import React, { useState, useEffect } from 'react'
import { Container, Row, Col, Form, Table, Badge } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { SearchResult } from '../types'
import { Logger } from '../utils/logger'

const Advanced: React.FC = () => {
  const [name, setName] = useState('')
  const [country, setCountry] = useState('')
  const [city, setCity] = useState('')
  const [result, setResult] = useState<SearchResult | null>(null)
  const [error, setError] = useState<string | null>(null)

  const advancedSearch = async (searchName?: string, searchCountry?: string, searchCity?: string) => {
    try {
      const currentName = searchName !== undefined ? searchName : name
      const currentCountry = searchCountry !== undefined ? searchCountry : country
      const currentCity = searchCity !== undefined ? searchCity : city
      
      const params = {
        from: 0,
        size: 10,
        country: currentCountry,
        city: currentCity,
        name: currentName
      }
      
      Logger.info(`Advanced search: name="${currentName}", country="${currentCountry}", city="${currentCity}"`)
      Logger.apiCall('GET', '/api/1/person/_advanced_search', params)
      const response = await axios.get('/api/1/person/_advanced_search', { params })
      
      Logger.apiResponse('GET', '/api/1/person/_advanced_search', response.data, response.data.took)
      Logger.info(`Advanced search completed: name="${currentName}", country="${currentCountry}", city="${currentCity}", results=${response.data.hits.total.value}`)
      
      setError(null)
      setResult(response.data)
    } catch (err) {
      Logger.apiError('GET', '/api/1/person/_advanced_search', err)
      Logger.error('Advanced search failed:', err)
      setError('Backend not available')
    }
  }

  useEffect(() => {
    Logger.info('Advanced component mounted, performing initial search')
    advancedSearch()
  }, [])

  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newName = e.target.value
    Logger.debug(`Name filter changed: "${name}" → "${newName}"${newName === '' ? ' (empty)' : ''}`)
    setName(newName)
    advancedSearch(newName, undefined, undefined)
  }

  const handleCountryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newCountry = e.target.value
    Logger.debug(`Country filter changed: "${country}" → "${newCountry}"${newCountry === '' ? ' (empty)' : ''}`)
    setCountry(newCountry)
    advancedSearch(undefined, newCountry, undefined)
  }

  const handleCityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newCity = e.target.value
    Logger.debug(`City filter changed: "${city}" → "${newCity}"${newCity === '' ? ' (empty)' : ''}`)
    setCity(newCity)
    advancedSearch(undefined, undefined, newCity)
  }

  return (
    <Container fluid>
      <Row>
        <Col>
          <Form.Floating className="mb-3">
            <Form.Control
              id="nameBox"
              type="text"
              placeholder="Name"
              value={name}
              onChange={handleNameChange}
              autoComplete="off"
            />
            <label htmlFor="nameBox">Name</label>
          </Form.Floating>
        </Col>
        <Col>
          <Form.Floating className="mb-3">
            <Form.Control
              id="countryBox"
              type="text"
              placeholder="Country"
              value={country}
              onChange={handleCountryChange}
              autoComplete="off"
            />
            <label htmlFor="countryBox">Country</label>
          </Form.Floating>
        </Col>
        <Col>
          <Form.Floating className="mb-3">
            <Form.Control
              id="cityBox"
              type="text"
              placeholder="City"
              value={city}
              onChange={handleCityChange}
              autoComplete="off"
            />
            <label htmlFor="cityBox">City</label>
          </Form.Floating>
        </Col>
      </Row>

      <Row>
        <Col>
          {!error && result && (
            <p>
              Found <Badge bg="primary">{result.hits.total.value}</Badge> hits in{' '}
              <Badge bg="primary">{result.took} ms</Badge>
            </p>
          )}
          {error && (
            <p>
              <Badge bg="danger">{error}</Badge>
            </p>
          )}
        </Col>
      </Row>

      <Row>
        <Col md={12}>
          <Table striped bordered hover className="table-condensed">
            <thead>
              <tr>
                <th>Name</th>
                <th>Gender</th>
                <th>Date Of Birth</th>
                <th>Country</th>
                <th>City</th>
              </tr>
            </thead>
            <tbody>
              {result?.hits.hits.map((entry) => (
                <tr key={entry._id}>
                  <td>
                    <Link to={`/person/${entry._id}`}>{entry._source.name}</Link>
                  </td>
                  <td>{entry._source.gender}</td>
                  <td>{entry._source.dateOfBirth}</td>
                  <td>{entry._source.address.country}</td>
                  <td>{entry._source.address.city}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Col>
      </Row>
    </Container>
  )
}

export default Advanced
