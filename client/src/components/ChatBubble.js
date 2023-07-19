import { Container, Button, List, ListItem, ListItemText, CircularProgress, Typography, Box, Grid, TextField, AppBar, Toolbar, Card, CardMedia, CardContent } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import Avatar from '@material-ui/core/Avatar';

const useStyles = makeStyles((theme) => ({
    chatBubble: {
        display: 'flex',
        alignItems: 'center',
        margin: theme.spacing(1),
        padding: theme.spacing(1),
        borderRadius: '10px',
        border: '1px solid #ccc',
        backgroundColor: '#f0f0f0',
    },
    chatIcon: {
        marginRight: theme.spacing(1),
    },
    chatUser: {
        fontWeight: 'bold',
    },
    chatDetails: {
        color: '#666',
    },
}));


const ChatBubble = ({history}) => {
    const classes = useStyles();
    return (
        <div className={classes.chatBubble}>
            <Avatar className={classes.chatIcon}>U</Avatar> 
            <div>
                <Typography variant="subtitle1" className={classes.chatUser}>
                    {history.user_name}
                </Typography>
                <Typography variant="subtitle2" className={classes.chatDetails}>
                    Time: {history.time} Price: ${history.price}
                </Typography>
            </div>
        </div>
    );
}

export default ChatBubble;