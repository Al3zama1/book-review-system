import React from "react";
import {HashRouter, Route, Routes} from "react-router-dom";
import {Container, Grid} from "semantic-ui-react";


import Header from "./Header";
import HomeContainer from './HomeContainer'
import AllReviewsContainer from "./AllReviewsContainer";

const App: React.FC = () => {
  return (
      <Container>
        <HashRouter>
          <Header/>
          <Grid centered>
              <Grid.Column width={10}>
                  <Routes>
                      <Route path="/" element={<HomeContainer />} />
                      <Route path="/all-reviews" element={<AllReviewsContainer />}/>
                  </Routes>
              </Grid.Column>
          </Grid>
        </HashRouter>
      </Container>
  );
}

export default App;
