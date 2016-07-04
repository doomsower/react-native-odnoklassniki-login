import React, {Component, PropTypes} from 'react';
import { View, StyleSheet } from 'react-native';
import Button from 'apsl-react-native-button';
import Odnoklassniki, { Scopes } from 'react-native-odnoklassniki-login';

class LogInScreen extends Component {
  static propTypes = {
    onAuth: PropTypes.func
  };

  render() {
    return (
      <View style={styles.container}>
        <Button style={styles.button} onPress={this.onLogin}>Log In</Button>
      </View>
    );
  }

  onLogin = () => {
    Odnoklassniki.login([Scopes.VALUABLE_ACCESS])
      .then(
        response => {
          console.log('Odnoklassniki login', response);
          this.props.onAuth(response);
        }
      )
      .catch(err => {
        console.log('Login error', err);
      })
  }
}

export default LogInScreen;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'stretch',
    justifyContent: 'center',
    padding: 24
  },
  button: {
    backgroundColor: 'orange',
    borderWidth: 0
  }
});