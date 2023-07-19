import React from 'react';
import { AppBar, Toolbar, Typography, makeStyles, Box, CircularProgress, Grid } from '@material-ui/core';
import TimerIcon from '@material-ui/icons/Timer';

const useStyles = makeStyles((theme) => ({
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
    position: 'fixed', // Fix the position
    top: 0, // Position it at the top
  },
  title: {
    flexGrow: 1,
  },
  user: {
    marginLeft: theme.spacing(2),
  },
  timer: {
    display: 'flex',
    alignItems: 'center',
    color: theme.palette.success.main, // Change color to green
  },
}));

const NavigationBar = ({ username, remainingTime }) => {
  const classes = useStyles();

  return (
    <AppBar className={classes.appBar}>
      <Toolbar>
        <Typography variant="h6" color="inherit" className={classes.title}>
          Auction App
        </Typography>
        <Grid container justify="center" alignItems="center" className={classes.timer}>
          <TimerIcon />
          <Typography variant="h6">
            {remainingTime} sec
          </Typography>
        </Grid>
        <Typography variant="h6"  color="inherit" className={classes.user}>
          Username: {username}
        </Typography>
      </Toolbar>
    </AppBar>
  );
}

export default NavigationBar;