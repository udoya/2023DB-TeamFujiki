import React, { useState, useEffect } from 'react';
import { Socket, io } from "socket.io-client";
import { Container, Button, List, ListItem, ListItemText, CircularProgress, Typography, Box, Grid, TextField, AppBar, Toolbar, Card, CardMedia, CardContent } from '@material-ui/core';
import { createTheme, ThemeProvider, makeStyles } from '@material-ui/core/styles';
import NavigationBar from './NavigationBar';
import PersonOutlineIcon from '@material-ui/icons/PersonOutline';
import { ClassRounded } from '@material-ui/icons';
import ChatBubbleOutlineIcon from '@material-ui/icons/ChatBubbleOutline';
import ChatBubble from './ChatBubble';
import LinearProgressWithLabel from './LinearProgressWithLabel';
const ENDPOINT = "http://localhost:10200";
const socket = io(ENDPOINT);
// const socket = io();
const DEFAULT_TIME = 120;

const theme = createTheme({
    palette: {
        primary: {
            main: '#000000',
        },
        secondary: {
            main: '#008000',
        },
    },
});

const useStyles = makeStyles((theme) => ({
    appBar: {
        marginBottom: theme.spacing(2),
    },
    itemImage: {
        width: '100%',
        height: 'auto',
    },
    auctionItem: {
        marginBottom: theme.spacing(2),
        marginLeft: theme.spacing(2),
        width: '100%',
    },
    auctionItemName: {
        textAlign: 'center',
        fontSize: 15,
    },
    bidButton: {
        marginTop: theme.spacing(2),
    },
    participantsContainer: {
        display: 'flex',
        alignItems: 'center',
        flexWrap: 'wrap',
    },
    participantIcon: {
        color: 'black',
        margin: theme.spacing(0.5),
    },
    media: {
        // height: 0,
        paddingTop: '100%', // 1:1 aspect ratio for images
        // backgroundSize: 'contain',
    },
    historyContainer: {
        maxHeight: '300px',
        overflowY: 'auto', // 縦スクロールを許可
        width: '100%',
    },
    itemContainer: {
        display: 'flex',
        overflowX: 'auto', // 横スクロールを許可
        width: '100%',
        maxHeight: '400px',
    },
    card: {
        width: 400,
        height: 400,
    },
    soldText: {
        color: 'red',
        textAlign: 'center',
        backgroundColor: '#f0f0f0',
        borderRadius: '10px',
        border: '1px solid #ccc',
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
    },
}));

function Auction() {
    let init_userItems = [
        {
            item_name:"Painting: Sunset on the Beach",
            item_id: 1,
            is_sold:0
        },
        {
            item_name:"Antique Pocket Watch: Gold-plated",
            item_id: 2,
            is_sold:0
        },
        {
            item_name:"Sports Memorabilia: Signed Baseball",
            item_id: 3,
            is_sold:1
        },
    ]
    let inituserId = 1;
    let initCurrentItem = {
        item_name:"Painting: Sunset on the Beach",
        item_id: 1,
        history: [
            {
                user_name: "user1",
                price:100,
                time:20
            },
            {   
                user_name: "user2",
                price:200,
                time:40
            },
            {
                user_name: "user3",
                price:300,
                time:60
            },
            {
                user_name: "user1",
                price:400,
                time:80
            }
        ]
    };
    let initNumParticipants = 5;
    let initExhibitorName = "exhibitor1";
    let isFirst = true;

    const classes = useStyles();
    const [username, setUsername] = useState('');
    const [userid, setUserId] = useState(1);
    const [userItems, setUserItems] = useState(null);
    const [remainingTime, setRemainingTime] = useState(100);
    const [inputBid, setInputBid] = useState(0);
    const [currentItem, setCurrentItem] = useState(null);
    const [auctionHistory, setAuctionHistory] = useState(null);
    const [price, setPrice] = useState(0);
    const [numParticipants, setNumParticipants] = useState(0);
    const [exhibitorName, setExhibitorName] = useState("John");
    const [isExhibitor, setIsExhibitor] = useState(false);
    const [finalResult, setFinalResult] = useState(null);


    const SocketConst = {
        INIT_STATE: 'init-state', 
        RAISE_HANDS: 'raise-hands',
        BID_ON: 'bid-on',
        SUCCESSFUL_BID: 'successful-bid',
        NOTIFY_NUM_OF_PARTICIPANTS: 'notify-num-of-participants'
    };

    const raiseHands = (item_id) => {
        //{item_id, user_id}
        if (isExhibitor) {
            alert("You are the exhibitor");
            return;
        }
        else if(remainingTime > 0){
            alert("Auction is not finished. Please wait for the result");
            return;
        }
        socket.emit(SocketConst.RAISE_HANDS, {item_id: item_id, user_id: userid});
    };

    useEffect(() => {
        if (isFirst) {
        const storedUsername = localStorage.getItem('username');
        setUsername(storedUsername || '');

        socket.on(SocketConst.INIT_STATE, (data) => {
            //{items, user_id, remaining_time, current_item, is_exhibitor, (auction_history) }
            console.log("ON: INIT_STATE");
            console.log("data", JSON.stringify(data))
            setUserItems(data.items);
            setUserId(data.user_id);
            // setRemainingTime(data.remaining_time);
            if (data.remainingTime < DEFAULT_TIME){
                setRemainingTime(DEFAULT_TIME - data.remaining_time);
            }
            setCurrentItem(data.current_item);
            setIsExhibitor(data.is_exhibitor);
            console.log("data.current_item.history", data.current_item.history)
        });

        socket.on(SocketConst.RAISE_HANDS, (data) => {
            //{item_id, item_name, user_id} 
            console.log("ON: RAISE_HANDS");
            setRemainingTime(DEFAULT_TIME);
            setPrice(0);
            setExhibitorName(data.user_name);
            setCurrentItem({item_id: data.item_id, item_name: data.item_name});
        });

        socket.on(SocketConst.BID_ON, (data) => {
            //{price, user_id, time}
            //もし全てのフィールドが0なら、入札失敗
            console.log("ON: BID_ON");
            if(data.price === 0 && data.user_id === 0 && data.time === 0){
                alert("Failed to bid");
            }else{
                setPrice(data.price);
                setRemainingTime(data.time);
                //setCurrentItemのhistory(配列)に新しい要素を追加
                setCurrentItem((currentItem) => {
                    return {...currentItem, history: [...currentItem.history, {user_id: data.user_id, price: data.price, time: data.time}]};
                });
            }
        });

        socket.on(SocketConst.SUCCESSFUL_BID, (data) => {
            //{price, user_id}
            //このイベントが発火された場合、数秒間結果の通知を表示した後で画面を更新する
            console.log("ON: SUCCESSFUL_BID");
            setFinalResult(data);
            setTimeout(()=>{
                alert("result : " + data.user_id + " won the auction with " + data.price + " yen");
                window.location.reload(false);
            }, 5000);
        });

        socket.on(SocketConst.NOTIFY_NUM_OF_PARTICIPANTS, (data) => {
            //{num_participants}
            console.log("ON: NOTIFY_NUM_OF_PARTICIPANTS");
            setNumParticipants(data.num_participants);
        });

            console.log("EMIT: INIT_STATE");
            socket.emit(SocketConst.INIT_STATE, {user_name: storedUsername});
            isFirst = false;
        }
    }, []);


    // Timer
    useEffect(() => {
        if (remainingTime > 0) {
            const interval = setInterval(() => {
                setRemainingTime((remainingTime) => remainingTime - 1);
            }, 1000);
            return () => clearInterval(interval);
        }
    }, [remainingTime]);

    const handleBid = () => {
        if(inputBid <= price){
            alert("Please input higher price");
            return;
        }
        if(inputBid > 0){
            //{price, user_id}
            socket.emit(SocketConst.BID_ON, {price: inputBid, user_id: userid});
        }
    };


    return (
        <>
        <ThemeProvider theme={theme}>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar>
                    <NavigationBar username={username} remainingTime={remainingTime} />
                </Toolbar>
            </AppBar>
            <LinearProgressWithLabel variant="determinate" value={remainingTime * (100/DEFAULT_TIME)} color={remainingTime <= 10 ? 'secondary' : 'primary'} />
            <Container maxWidth="lg">
                <Grid container spacing={1}>
                    <Grid item xs={12} md={6}>
                        { currentItem != null && (
                        <>
                        <Typography variant="h5" gutterBottom>
                        Current Item
                        </Typography>
                        <Card className={classes.card}>
                        <CardMedia
                            className={classes.media}
                            image={`/images/${userid}_${currentItem.item_id}.png`}
                            title={currentItem.item_name}
                        />
                        <CardContent>
                            <Typography variant="h5" component="div">
                            {currentItem.item_name}
                            </Typography>
                        </CardContent>
                        </Card>
                        <Typography variant="body1" gutterBottom>
                            Exhibitor: {exhibitorName}
                        </Typography>
                        <Typography variant="h6" gutterBottom>
                            Current Price: {price + " "} yen
                        </Typography>
                        <div className={classes.participantsContainer}>
                            {Array(numParticipants).fill().map((_, i) => (
                                <PersonOutlineIcon key={i} className={classes.participantIcon} />
                            ))}
                        </div>
                        <TextField 
                            variant="outlined"
                            type="number"
                            label="Bid Amount"
                            value={inputBid}
                            onChange={(event) => setInputBid(event.target.value)}
                        />
                        <Button variant="contained" color="secondary" className={classes.bidButton} onClick={handleBid}>
                            Bid
                        </Button></>)}
                    </Grid>
                    <Grid item xs={12} sm={6}>
                    <Typography variant="h5" gutterBottom>
                        Auction History
                    </Typography>
                    { currentItem != null && currentItem.history.length != 0 && (
                    <div className={classes.historyContainer}>
                        {currentItem.history.map((history, index) => (
                            <ChatBubble key={index} history={history} />
                        ))}
                    </div>)}
                    <Typography variant="h5" gutterBottom>
                        My Items
                    </Typography>
                    { userItems != null && userItems.length != 0 && (
                    <Box className={classes.itemContainer}>
                        {userItems.map((item, index) => (
                            <Card key={index} className={classes.auctionItem}>
                            <CardMedia
                            className={classes.media}
                            image={`/images/${userid}_${item.item_id}.png`}
                            title={item.item_name}
                            />
                            <CardContent>
                            <Typography variant="h6" gutterBottom className={classes.auctionItemName}>
                                {item.item_name}
                            </Typography>
                            {item.is_sold == 0 ? (
                            <Button variant="contained" color="primary" onClick={() => {raiseHands(item.item_id)}}>
                                Start Auction
                            </Button>
                            ) : (
                                <Typography variant="body1" gutterBottom className={classes.soldText}>
                                    Already Sold
                                </Typography>
                            )}
                            </CardContent>
                        </Card>
                        ))}
                        </Box>)}
                    </Grid>
                </Grid>
            </Container>
        </ThemeProvider>
        </>
    );
}

export default Auction;