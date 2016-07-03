import React, {Component} from 'react';
import LoggedInScreen from './LoggedInScreen';
import LogInScreen from './LogInScreen';
import Odnoklassniki from './Odnoklassniki';

class App extends Component {

  constructor(props) {
    super(props);
    this.state = {
      auth: null
    }
  }

  componentWillMount() {
    console.log('App will mount');
    Odnoklassniki.initialize('1247442432', 'CBAHHLFLEBABABABA');
    /*
    Odnoklassniki.isLoggedIn()            
      .then(resp => {
          console.log('Is logged in: ', resp);
          this.setState({auth: resp});
      })
      */
  }

  render() {
    if (this.state.auth)
      return <LoggedInScreen auth={this.state.auth} onAuth={this.onAuth} />;
    else
      return <LogInScreen onAuth={this.onAuth}/>
  }

  onAuth = auth => {
    this.setState({auth});
  };
}

export default App;