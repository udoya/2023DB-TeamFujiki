import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TextField, Button, Container, Box, Grid, AppBar, Toolbar, Typography } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
  button: {
    height: '56px', // Manually adjust the button's height
    // margin: theme.spacing(2),
  },
}));

function Login() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const classes = useStyles();

  const handleUsernameChange = (event) => {
    setUsername(event.target.value);
  };

  const login = () => {
    localStorage.setItem('username', username);
    navigate('/home');
  };

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" color="inherit">
            Auction App
          </Typography>
        </Toolbar>
      </AppBar>
      <Grid
        container
        style={{ minHeight: 'calc(100vh - 64px)' }} // Adjust for AppBar height
        direction="column"
        justifyContent="center"
        alignItems="center"
      >
        <Container maxWidth="sm">
          <Grid container spacing={1} alignItems="flex-end">
            <Grid item xs>
              <TextField
                label="Username"
                variant="outlined"
                value={username}
                onChange={handleUsernameChange}
                fullWidth
              />
            </Grid>
            <Grid item>
              <Button 
                variant="contained" 
                color="primary" 
                onClick={login} 
                className={classes.button} // Apply the custom style
              >
                Login
              </Button>
            </Grid>
          </Grid>
        </Container>
      </Grid>
    </>
  );
}

export default Login;