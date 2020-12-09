
TableHighlighter = Class.create();
TableHighlighter.prototype = {

    initialize: function(id, decalx, decaly) {
        this.table = $(id);
        this.decalx = decalx;
        this.decaly = decaly;
        var trs = $$('#'+this.table.id+' tr');
        for (p=this.decaly;p<trs.length;++p){
            this.scan(trs[p]);
        }
    },

    scan: function(tr) {
        var element = $(tr);
        var descendants = element.getElementsByTagName('input');
        for(q=0;q<descendants.length;++q) {
            var td = $(descendants[q]);
            td.observe('mouseover', this.highlight.bind(this));
            td.observe('mouseout', this.highlight.bind(this));
        }
    },

    highlight: function(e) {
        var td = Event.element(e).parentNode;
        var tr = td.parentNode;
        var trs = $$('#'+this.table.id+' tr');
        var position = td.previousSiblings().length;

        for (p=this.decaly-1;p<trs.length;++p){
            var element = $(trs[p]);
            var num = position;
            if(p==1) num = num - this.decalx;
            element.immediateDescendants()[num ].toggleClassName('highlighted');
        }
        tr.toggleClassName('highlighted');
    }

};


function resizableGrid(table) {
    var row = table.getElementsByTagName('tr')[0],
    cols = row ? row.children : undefined;
    if (!cols) return;

    table.style.overflow = 'hidden';

    var tableHeight = table.offsetHeight;

    for (var i=0;i<cols.length;i++){
        var div = createDiv(tableHeight);
        cols[i].appendChild(div);
        cols[i].style.position = 'relative';
        setListeners(div);
    }

    function setListeners(div){
        var pageX,curCol,nxtCol,curColWidth,nxtColWidth;

        div.addEventListener('mousedown', function (e) {
            curCol = e.target.parentElement;
            nxtCol = curCol.nextElementSibling;
            pageX = e.pageX;

            var padding = paddingDiff(curCol);

            curColWidth = curCol.offsetWidth - padding;
            if (nxtCol)
                nxtColWidth = nxtCol.offsetWidth - padding;
        });

        div.addEventListener('mouseover', function (e) {
            e.target.style.borderRight = '2px solid #454545';
        })

        div.addEventListener('mouseout', function (e) {
            e.target.style.borderRight = '';
        })

        document.addEventListener('mousemove', function (e) {
            if (curCol) {
                var diffX = e.pageX - pageX;

                if (nxtCol)
                 nxtCol.style.width = (nxtColWidth - (diffX))+'px';

                curCol.style.width = (curColWidth + diffX)+'px';
            }
        });

        document.addEventListener('mouseup', function (e) {
            curCol = undefined;
            nxtCol = undefined;
            pageX = undefined;
            nxtColWidth = undefined;
            curColWidth = undefined
        });
    }

    function createDiv(height){
        var div = document.createElement('div');
        div.style.top = 0;
        div.style.right = 0;
        div.style.width = '5px';
        div.style.position = 'absolute';
        div.style.cursor = 'col-resize';
        div.style.userSelect = 'none';
        div.style.height = height + 'px';
        return div;
    }

    function paddingDiff(col){
        if (getStyleVal(col,'box-sizing') == 'border-box'){
            return 0;
        }

        var padLeft = getStyleVal(col,'padding-left');
        var padRight = getStyleVal(col,'padding-right');
        return (parseInt(padLeft) + parseInt(padRight));
    }

    function getStyleVal(elm,css){
        return (window.getComputedStyle(elm, null).getPropertyValue(css))
    }
};