import React from "react";
import {HashRouter, Route, Routes} from "react-router-dom";
import {Container, Grid} from "semantic-ui-react";

import Header from "./Header";

const App: React.FC = () => {
  return (
      <Container>
        <HashRouter>
          <Header/>
          <Grid centered>

          </Grid>
        </HashRouter>
      </Container>
  );
}

export default App;
