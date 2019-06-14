var tabIdToPreviousUrl = {};

// function writeToDB(input){
//     $.ajax({
//         type: 'post',
//         url: 'https://localhost/db1/script.php',
//         data: {input: input},
//         success: function(){
//             alert("it workin'");
//         }
//     })
// }

var switchStatus = false;
chrome.storage.local.get('sS', function(status){
    switchStatus = status.sS;
});

chrome.storage.onChanged.addListener(function() {
    chrome.storage.local.get('sS', function(status){
        switchStatus = status.sS;
    });
});

var statusW = '';
chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab){
    if(switchStatus === true){
        // alert(changeInfo.status);
        // if(changeInfo.status === 'loading'){
            var previousUrl = "";
            previousUrl = tabIdToPreviousUrl[tabId];
            var preU = ''; var newU = '';
            if(previousUrl === undefined){
                preU = 'undefined';
            }
            if(!(previousUrl === undefined)){
                var array1 = previousUrl.split('/');
                preU = array1[2];
            }
            var array2 = (changeInfo.url).split('/');
            /*
                ISSUE!!!!
                Right here changeInfo.status forgets the status 'complete'
                Need to research this so that we can have our on complete work also
            */
            newU = array2[2];
            if((preU !== newU)&&(!(preU==='undefined'))&&(!(preU==='newtab'))&&(!(newU==='newtab'))){
                var sendEnd = tabId + "," + preU + ",0," + Date.now();
                alert(sendEnd);
                //writeToDB(sendEnd);
            }
            if((preU !== newU)&&(!(newU==='newtab'))&&(changeInfo.status === 'loading')){
                var sendStart = tabId + "," + newU + ",1," + Date.now();
                alert(sendStart);
                //writeToDB(sendStart);
            }
            if((!(newU==='newtab'))&&(changeInfo.status === 'complete')){
                var sendDone = tabId + "," + newU + ",2," + Date.now();
                alert(sendDone);
                //writeToDB(sendStart);
            }
            tabIdToPreviousUrl[tabId] = changeInfo.url;
        // }
    }
});

chrome.tabs.onRemoved.addListener(function(tabId, removeInfo){
    if(switchStatus === true){
        var previousUrl = "";
        previousUrl = tabIdToPreviousUrl[tabId];
        var preU = ''; 
        if(previousUrl === undefined){
            preU = 'undefined';
        }
        if(!(previousUrl === undefined)){
            var array1 = previousUrl.split('/');
            preU = array1[2];
        }
        if(!(preU === 'newtab')&&(!(preU === undefined))){
            var sendClosed = tabId + "," + preU + ",0," + Date.now();
            alert(sendClosed);
            //writeToDB(sendClosed);
        }
    }
});