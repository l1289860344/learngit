markers.forEach(function(marker) {
    var markerSingle =  new AMap.Marker({
        map: map,
        position: [marker.lnglat[0], marker.lnglat[1]],
        offset: new AMap.Pixel(-13, -30),
        extData:"当前500米范围内有至少"+marker.count+"件"
    });

    var factor = Math.pow((marker.count*100) / 3899999, 1 / 18);
    var div = document.createElement('div');
    var Hue = 180 - factor * 180;
    var bgColor = 'hsla(' + Hue + ',100%,50%,0.7)';
    var fontColor = 'hsla(' + Hue + ',100%,20%,1)';
    var borderColor = 'hsla(' + Hue + ',100%,40%,1)';
    var shadowColor = 'hsla(' + Hue + ',100%,50%,1)';
    div.style.backgroundColor = bgColor;

    var size = Math.round(30 + Math.pow((marker.count + 500) / 3899999, 1 / 5) * 20);
    // console.log(marker)
    // console.log(size);
    // console.log(marker.count);
    div.style.width = div.style.height = size + 'px';
    div.style.border = 'solid 1px ' + borderColor;
    div.style.borderRadius = size / 2 + 'px';
    div.style.boxShadow = '0 0 1px ' + shadowColor;
    div.innerHTML = marker.count;
    div.style.lineHeight = size + 'px';
    div.style.color = fontColor;
    div.style.fontSize = '14px';
    div.style.textAlign = 'center';
    markerSingle.on('click', showInfoM);
    markerSingle.setOffset(new AMap.Pixel(-size / 2, -size / 2));
    markerSingle.setContent(div)
});
 function showInfoM(e){
        console.log(e)
        console.log(e.target.getExtData())
        document.getElementById('centerCoord').innerHTML = e.target.getExtData();
        document.getElementById('tips').innerHTML = '坐标为:'+e.lnglat.lng+','+e.lnglat.lat;
}