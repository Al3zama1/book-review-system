import React from "react";
import {HashRouter, Route, Routes} from "react-router-dom";
import {Container, Grid} from "semantic-ui-react";


import Header from "./Header";
import HomeContainer from './HomeContainer'

const App: React.FC = () => {
  return (
      <Container>
        <HashRouter>
          <Header/>
          <Grid centered>
              <Grid.Column width={10}>
                  <Routes>
                      <Route path="/" element={<HomeContainer />} />
                  </Routes>
              </Grid.Column>
          </Grid>
        </HashRouter>
      </Container>
  );
}

export default App;
