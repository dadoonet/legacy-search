import { useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'
import { Container, Navbar, Nav } from 'react-bootstrap'
import Search from './components/Search'
import Init from './components/Init'
import Advanced from './components/Advanced'
import Compute from './components/Compute'
import Kibana from './components/Kibana'
import PersonDetail from './components/PersonDetail'
import { Logger } from './utils/logger'

function App() {
  useEffect(() => {
    Logger.info('🚀 Legacy Search React App started!')
    Logger.info('Using React 18 with TypeScript')
    Logger.debug('Available routes: /, /search, /init, /advanced, /compute, /kibana, /person/:id')
  }, [])
  return (
    <Router>
      <div className="App">
        <Navbar bg="light" expand="lg">
          <Container fluid>
            <Navbar.Brand href="#">People</Navbar.Brand>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
              <Nav className="me-auto">
                <Nav.Link as={Link} to="/init">Init</Nav.Link>
                <Nav.Link as={Link} to="/">Search</Nav.Link>
                <Nav.Link as={Link} to="/advanced">Advanced</Nav.Link>
                <Nav.Link as={Link} to="/compute">Compute</Nav.Link>
              </Nav>
              <Nav className="justify-content-end">
                <Nav.Link as={Link} to="/kibana">Kibana</Nav.Link>
              </Nav>
            </Navbar.Collapse>
          </Container>
        </Navbar>

        <Routes>
          <Route path="/" element={<Search />} />
          <Route path="/search" element={<Search />} />
          <Route path="/init" element={<Init />} />
          <Route path="/advanced" element={<Advanced />} />
          <Route path="/compute" element={<Compute />} />
          <Route path="/kibana" element={<Kibana />} />
          <Route path="/person/:id" element={<PersonDetail />} />
        </Routes>
      </div>
    </Router>
  )
}

export default App
