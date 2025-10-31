import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Container, Card, Row, Col, Form, Button } from 'react-bootstrap'
import axios from 'axios'
import { Logger } from '../utils/logger'

interface PersonDetailData {
  reference?: string;
  gender: string;
  name: string;
  dateOfBirth: string;
  address: {
    city: string;
    country: string;
  };
  marketing: {
    cars: number;
    shoes: number;
    toys: number;
    fashion: number;
    music: number;
    garden: number;
    electronic: number;
    hifi: number;
    food: number;
  };
}

const PersonDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  
  const [person, setPerson] = useState<PersonDetailData>({
    gender: '',
    name: '',
    dateOfBirth: '',
    address: {
      city: '',
      country: ''
    },
    marketing: {
      cars: 0,
      shoes: 0,
      toys: 0,
      fashion: 0,
      music: 0,
      garden: 0,
      electronic: 0,
      hifi: 0,
      food: 0
    }
  })

  useEffect(() => {
    if (id) {
      Logger.info(`PersonDetail component mounted for person ID: ${id}`)
      loadPerson()
    }
  }, [id])

  const loadPerson = async () => {
    try {
      Logger.apiCall('GET', `/api/1/person/${id}`)
      const response = await axios.get(`/api/1/person/${id}`)
      
      Logger.apiResponse('GET', `/api/1/person/${id}`, response.data)
      Logger.info(`Person loaded: ${response.data.name} (${response.data.gender})`)
      
      setPerson(response.data)
    } catch (error) {
      Logger.apiError('GET', `/api/1/person/${id}`, error)
      Logger.error('Error loading person:', error)
    }
  }

  const handleSave = async () => {
    try {
      Logger.apiCall('PUT', `/api/1/person/${id}`, person)
      await axios.put(`/api/1/person/${id}`, person)
      
      Logger.apiResponse('PUT', `/api/1/person/${id}`, { success: true })
      Logger.info(`✅ Person saved successfully: ${person.name}`)
    } catch (error) {
      Logger.apiError('PUT', `/api/1/person/${id}`, error)
      Logger.error('❌ Error saving person:', error)
    }
  }

  const handleDelete = async () => {
    try {
      Logger.apiCall('DELETE', `/api/1/person/${id}`)
      await axios.delete(`/api/1/person/${id}`)
      
      Logger.apiResponse('DELETE', `/api/1/person/${id}`, { success: true })
      Logger.info(`✅ Person deleted successfully: ${person.name}`)
      
      navigate('/')
    } catch (error) {
      Logger.apiError('DELETE', `/api/1/person/${id}`, error)
      Logger.error('❌ Error deleting person:', error)
    }
  }

  const handleInputChange = (field: string, value: string | number) => {
    setPerson(prev => {
      const keys = field.split('.')
      if (keys.length === 1) {
        return { ...prev, [field]: value }
      } else if (keys.length === 2) {
        return {
          ...prev,
          [keys[0]]: {
            ...prev[keys[0] as keyof PersonDetailData],
            [keys[1]]: value
          }
        }
      }
      return prev
    })
  }

  return (
    <Container fluid>
      <form id="personForm">
        <input type="hidden" name="reference" value={person.reference || ''} />

        <Card className="mb-3">
          <Card.Header>
            <h4>Person Information</h4>
          </Card.Header>
          <Card.Body>
            <Row>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="gender">Gender</Form.Label>
                  <Form.Select
                    id="gender"
                    value={person.gender}
                    onChange={(e) => handleInputChange('gender', e.target.value)}
                  >
                    <option value="">Select...</option>
                    <option value="female">Female</option>
                    <option value="male">Male</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="name">Name</Form.Label>
                  <Form.Control
                    type="text"
                    id="name"
                    value={person.name}
                    onChange={(e) => handleInputChange('name', e.target.value)}
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="dob">Date of Birth</Form.Label>
                  <Form.Control
                    type="text"
                    id="dob"
                    value={person.dateOfBirth}
                    onChange={(e) => handleInputChange('dateOfBirth', e.target.value)}
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
            </Row>
            <Row>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="city">City</Form.Label>
                  <Form.Control
                    type="text"
                    id="city"
                    value={person.address.city}
                    onChange={(e) => handleInputChange('address.city', e.target.value)}
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="country">Country</Form.Label>
                  <Form.Control
                    type="text"
                    id="country"
                    value={person.address.country}
                    onChange={(e) => handleInputChange('address.country', e.target.value)}
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        <Card className="mb-3">
          <Card.Header>
            <h4>Marketing data</h4>
          </Card.Header>
          <Card.Body>
            <Row>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.cars">Cars</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.cars"
                    value={person.marketing.cars}
                    onChange={(e) => handleInputChange('marketing.cars', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.shoes">Shoes</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.shoes"
                    value={person.marketing.shoes}
                    onChange={(e) => handleInputChange('marketing.shoes', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.toys">Toys</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.toys"
                    value={person.marketing.toys}
                    onChange={(e) => handleInputChange('marketing.toys', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
            </Row>
            <Row>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.fashion">Fashion</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.fashion"
                    value={person.marketing.fashion}
                    onChange={(e) => handleInputChange('marketing.fashion', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.music">Music</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.music"
                    value={person.marketing.music}
                    onChange={(e) => handleInputChange('marketing.music', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.garden">Garden</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.garden"
                    value={person.marketing.garden}
                    onChange={(e) => handleInputChange('marketing.garden', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
            </Row>
            <Row>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.electronics">Electronics</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.electronics"
                    value={person.marketing.electronic}
                    onChange={(e) => handleInputChange('marketing.electronic', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.hifi">Hifi</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.hifi"
                    value={person.marketing.hifi}
                    onChange={(e) => handleInputChange('marketing.hifi', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group>
                  <Form.Label htmlFor="marketing.food">Food</Form.Label>
                  <Form.Control
                    type="number"
                    id="marketing.food"
                    value={person.marketing.food}
                    onChange={(e) => handleInputChange('marketing.food', parseInt(e.target.value) || 0)}
                    min="0"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        <Button variant="primary" onClick={handleSave} className="me-2">
          Save changes
        </Button>
        <Button variant="danger" onClick={handleDelete}>
          Delete
        </Button>
      </form>
    </Container>
  )
}

export default PersonDetail
