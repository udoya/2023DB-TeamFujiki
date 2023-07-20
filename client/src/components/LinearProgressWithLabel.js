import React from 'react';
import LinearProgress from '@material-ui/core/LinearProgress';
import { makeStyles, Typography, Box } from '@material-ui/core';

const useStyles = makeStyles((theme) => ({
  root: {
    position: 'fixed',
    width: '100%',
    zIndex: theme.zIndex.drawer + 1,
    top: '59px', // Position it right below the AppBar
  },
  colorPrimary: {
    backgroundColor: theme.palette.success.light, // Change color to green
  },
  barColorPrimary: {
    backgroundColor: theme.palette.success.main, // Change color to green
  },
}));

export default function LinearProgressWithLabel(props) {
  const classes = useStyles();

  return (
    <Box display="flex" alignItems="center" className={classes.root}>
      <Box width="100%" mr={1}>
        <LinearProgress variant="determinate" {...props} classes={{colorPrimary: classes.colorPrimary, barColorPrimary: classes.barColorPrimary}} />
      </Box>
      <Box minWidth={35}>
        <Typography variant="body2" color="textSecondary">{`${Math.round(props.value,)}%`}</Typography>
      </Box>
    </Box>
  );
}