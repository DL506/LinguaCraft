import{$t as e,Bt as t,C as n,E as r,F as i,Ft as a,J as o,K as s,M as c,P as l,Pt as u,Qn as d,Rt as f,T as p,Tn as m,Wt as h,Y as g,an as _,dn as v,et as y,in as b,rn as x,sn as S,w as C}from"./light-TBGKFU-e.js";import{t as w}from"./format-length-BwSo_EUP.js";import{t as T}from"./light-Cc7NzxCE.js";var E=[`id`],D=[`stop-color`],O=[`stop-color`],k=[`viewBox`],A=[`d`,`stroke-width`],j=[`d`,`stroke-width`],M={success:(m(),_(C)),error:(m(),_(r)),warning:(m(),_(n)),info:(m(),_(p))},N=v({name:`ProgressCircle`,props:{clsPrefix:{type:String,required:!0},status:{type:String,required:!0},strokeWidth:{type:Number,required:!0},fillColor:[String,Object],railColor:String,railStyle:[String,Object],percentage:{type:Number,default:0},offsetDegree:{type:Number,default:0},showIndicator:{type:Boolean,required:!0},indicatorTextColor:String,unit:String,viewBoxWidth:{type:Number,required:!0},gapDegree:{type:Number,required:!0},gapOffsetDegree:{type:Number,default:0}},setup(t,{slots:n}){let r=x(()=>{let e=`gradient`,{fillColor:n}=t;return typeof n==`object`?`${e}-${h(JSON.stringify(n))}`:e});function i(e,n,i,a){let{gapDegree:o,viewBoxWidth:s,strokeWidth:c}=t,l=50+c/2,u=`M ${l},${l} m 0,50
      a 50,50 0 1 1 0,-100
      a 50,50 0 1 1 0,100`,d=Math.PI*2*50;return{pathString:u,pathStyle:{stroke:a===`rail`?i:typeof t.fillColor==`object`?`url(#${r.value})`:i,strokeDasharray:`${Math.min(e,100)/100*(d-o)}px ${s*8}px`,strokeDashoffset:`-${o/2}px`,transformOrigin:n?`center`:void 0,transform:n?`rotate(${n}deg)`:void 0}}}let a=()=>{let e=typeof t.fillColor==`object`,n=e?t.fillColor.stops[0]:``,i=e?t.fillColor.stops[1]:``;return e&&(m(),S(`defs`,null,[b(`linearGradient`,{id:r.value,x1:`0%`,y1:`100%`,x2:`100%`,y2:`0%`},[b(`stop`,{offset:`0%`,"stop-color":n},null,8,D),b(`stop`,{offset:`100%`,"stop-color":i},null,8,O)],8,E)]))};return()=>{let{fillColor:r,railColor:o,strokeWidth:l,offsetDegree:u,status:f,percentage:p,showIndicator:h,indicatorTextColor:v,unit:y,gapOffsetDegree:x,clsPrefix:C}=t,{pathString:w,pathStyle:T}=i(100,0,o,`rail`),{pathString:E,pathStyle:D}=i(p,u,r,`fill`),O=100+l;return m(),S(`div`,{class:s(`${C}-progress-content`),role:`none`},[b(`div`,{class:s(`${C}-progress-graph`),"aria-hidden":!0},[b(`div`,{class:s(`${C}-progress-graph-circle`),style:d({transform:x?`rotate(${x}deg)`:void 0})},[(m(),S(`svg`,{viewBox:`0 0 ${O} ${O}`},[g(()=>a()),b(`g`,null,[b(`path`,{class:s(`${C}-progress-graph-circle-rail`),d:w,"stroke-width":l,"stroke-linecap":`round`,fill:`none`,style:d(T)},null,14,A)]),b(`g`,null,[b(`path`,{class:s([`${C}-progress-graph-circle-fill`,p===0&&`${C}-progress-graph-circle-fill--empty`]),d:E,"stroke-width":l,"stroke-linecap":`round`,fill:`none`,style:d(D)},null,14,j)])],8,k))],6)],2),h?(m(),S(`div`,{key:0},[n.default?(m(),S(`div`,{key:0,class:s(`${C}-progress-custom-content`),role:`none`},[g(()=>n.default())],2)):(m(),S(e,{key:1},[f==="default"?(m(),S(`div`,{key:1,class:s(`${C}-progress-text`),style:d({color:v}),role:`none`},[b(`span`,{class:s(`${C}-progress-text__percentage`)},[g(()=>p)],2),b(`span`,{class:s(`${C}-progress-text__unit`)},[g(()=>y)],2)],6)):(m(),S(`div`,{key:0,class:s(`${C}-progress-icon`),"aria-hidden":!0},[(m(),_(c,{clsPrefix:C},{default:()=>M[f]},1032,[`clsPrefix`]))],2))],64))])):g(()=>null)],2)}}}),P={success:(m(),_(C)),error:(m(),_(r)),warning:(m(),_(n)),info:(m(),_(p))},F=v({name:`ProgressLine`,props:{clsPrefix:{type:String,required:!0},percentage:{type:Number,default:0},railColor:String,railStyle:[String,Object],fillColor:[String,Object],status:{type:String,required:!0},indicatorPlacement:{type:String,required:!0},indicatorTextColor:String,unit:{type:String,default:`%`},processing:{type:Boolean,required:!0},showIndicator:{type:Boolean,required:!0},height:[String,Number],railBorderRadius:[String,Number],fillBorderRadius:[String,Number]},setup(t,{slots:n}){let r=x(()=>w(t.height)),i=x(()=>typeof t.fillColor==`object`?`linear-gradient(to right, ${t.fillColor?.stops[0]} , ${t.fillColor?.stops[1]})`:t.fillColor),a=x(()=>t.railBorderRadius===void 0?t.height===void 0?``:w(t.height,{c:.5}):w(t.railBorderRadius)),o=x(()=>t.fillBorderRadius===void 0?t.railBorderRadius===void 0?t.height===void 0?``:w(t.height,{c:.5}):w(t.railBorderRadius):w(t.fillBorderRadius));return()=>{let{indicatorPlacement:l,railColor:u,railStyle:f,percentage:p,unit:h,indicatorTextColor:v,status:y,showIndicator:x,processing:C,clsPrefix:w}=t;return m(),S(`div`,{class:s(`${w}-progress-content`),role:`none`},[b(`div`,{class:s(`${w}-progress-graph`),"aria-hidden":!0},[b(`div`,{class:s([`${w}-progress-graph-line`,{[`${w}-progress-graph-line--indicator-${l}`]:!0}])},[b(`div`,{class:s(`${w}-progress-graph-line-rail`),style:d([{backgroundColor:u,height:r.value,borderRadius:a.value},f])},[b(`div`,{class:s([`${w}-progress-graph-line-fill`,C&&`${w}-progress-graph-line-fill--processing`]),style:d({maxWidth:`${t.percentage}%`,background:i.value,height:r.value,lineHeight:r.value,borderRadius:o.value})},[l===`inside`?(m(),S(`div`,{key:0,class:s(`${w}-progress-graph-line-indicator`),style:d({color:v})},[n.default?(m(),S(e,{key:0},[g(()=>n.default())],64)):(m(),S(e,{key:1},[g(()=>`${p}${h}`)],64))],6)):g(()=>null)],6)],6)],2)],2),x&&l===`outside`?(m(),S(`div`,{key:0},[n.default?(m(),S(`div`,{key:0,class:s(`${w}-progress-custom-content`),style:d({color:v}),role:`none`},[g(()=>n.default())],6)):(m(),S(e,{key:1},[y==="default"?(m(),S(`div`,{key:0,role:`none`,class:s(`${w}-progress-icon ${w}-progress-icon--as-text`),style:d({color:v})},[g(()=>p),g(()=>h)],6)):(m(),S(`div`,{key:1,class:s(`${w}-progress-icon`),"aria-hidden":!0},[(m(),_(c,{clsPrefix:w},{default:()=>P[y]},1032,[`clsPrefix`]))],2))],64))])):g(()=>null)],2)}}}),I=[`id`],L=[`stop-color`],R=[`stop-color`],z=[`d`,`stroke-width`],B=[`d`,`stroke-width`],V=[`viewBox`];function H(e,t,n=100){return`m ${n/2} ${n/2-e} a ${e} ${e} 0 1 1 0 ${2*e} a ${e} ${e} 0 1 1 0 -${2*e}`}var U=v({name:`ProgressMultipleCircle`,props:{clsPrefix:{type:String,required:!0},viewBoxWidth:{type:Number,required:!0},percentage:{type:Array,default:[0]},strokeWidth:{type:Number,required:!0},circleGap:{type:Number,required:!0},showIndicator:{type:Boolean,required:!0},fillColor:{type:Array,default:()=>[]},railColor:{type:Array,default:()=>[]},railStyle:{type:Array,default:()=>[]}},setup(e,{slots:t}){let n=x(()=>e.percentage.map((t,n)=>`${Math.PI*t/100*(e.viewBoxWidth/2-e.strokeWidth/2*(1+2*n)-e.circleGap*n)*2}, ${e.viewBoxWidth*8}`)),r=(t,n)=>{let r=e.fillColor[n],i=typeof r==`object`?r.stops[0]:``,a=typeof r==`object`?r.stops[1]:``;return typeof e.fillColor[n]==`object`&&(m(),S(`linearGradient`,{id:`gradient-${n}`,x1:`100%`,y1:`0%`,x2:`0%`,y2:`100%`},[b(`stop`,{offset:`0%`,"stop-color":i},null,8,L),b(`stop`,{offset:`100%`,"stop-color":a},null,8,R)],8,I))};return()=>{let{viewBoxWidth:i,strokeWidth:a,circleGap:o,showIndicator:c,fillColor:l,railColor:u,railStyle:f,percentage:p,clsPrefix:h}=e;return m(),S(`div`,{class:s(`${h}-progress-content`),role:`none`},[b(`div`,{class:s(`${h}-progress-graph`),"aria-hidden":!0},[b(`div`,{class:s(`${h}-progress-graph-circle`)},[(m(),S(`svg`,{viewBox:`0 0 ${i} ${i}`},[b(`defs`,null,[g(()=>p.map((e,t)=>r(e,t)))]),g(()=>p.map((e,t)=>(m(),S(`g`,{key:t},[b(`path`,{class:s(`${h}-progress-graph-circle-rail`),d:H(i/2-a/2*(1+2*t)-o*t,a,i),"stroke-width":a,"stroke-linecap":`round`,fill:`none`,style:d([{strokeDashoffset:0,stroke:u[t]},f[t]])},null,14,z),b(`path`,{class:s([`${h}-progress-graph-circle-fill`,e===0&&`${h}-progress-graph-circle-fill--empty`]),d:H(i/2-a/2*(1+2*t)-o*t,a,i),"stroke-width":a,"stroke-linecap":`round`,fill:`none`,style:d({strokeDasharray:n.value[t],strokeDashoffset:0,stroke:typeof l[t]==`object`?`url(#gradient-${t})`:l[t]})},null,14,B)]))))],8,V))],2)],2),c&&t.default?(m(),S(`div`,{key:0},[b(`div`,{class:s(`${h}-progress-text`)},[g(()=>t.default())],2)])):g(()=>null)],2)}}}),W=u([a(`progress`,{display:`inline-block`},[a(`progress-icon`,`
 color: var(--n-icon-color);
 transition: color .3s var(--n-bezier);
 `),f(`line`,`
 width: 100%;
 display: block;
 `,[a(`progress-content`,`
 display: flex;
 align-items: center;
 `,[a(`progress-graph`,{flex:1})]),a(`progress-custom-content`,{marginLeft:`14px`}),a(`progress-icon`,`
 width: 30px;
 padding-left: 14px;
 height: var(--n-icon-size-line);
 line-height: var(--n-icon-size-line);
 font-size: var(--n-icon-size-line);
 `,[f(`as-text`,`
 color: var(--n-text-color-line-outer);
 text-align: center;
 width: 40px;
 font-size: var(--n-font-size);
 padding-left: 4px;
 transition: color .3s var(--n-bezier);
 `)])]),f(`circle, dashboard`,{width:`120px`},[a(`progress-custom-content`,`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 justify-content: center;
 `),a(`progress-text`,`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 color: inherit;
 font-size: var(--n-font-size-circle);
 color: var(--n-text-color-circle);
 font-weight: var(--n-font-weight-circle);
 transition: color .3s var(--n-bezier);
 white-space: nowrap;
 `),a(`progress-icon`,`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 color: var(--n-icon-color);
 font-size: var(--n-icon-size-circle);
 `)]),f(`multiple-circle`,`
 width: 200px;
 color: inherit;
 `,[a(`progress-text`,`
 font-weight: var(--n-font-weight-circle);
 color: var(--n-text-color-circle);
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 justify-content: center;
 transition: color .3s var(--n-bezier);
 `)]),a(`progress-content`,{position:`relative`}),a(`progress-graph`,{position:`relative`},[a(`progress-graph-circle`,[u(`svg`,{verticalAlign:`bottom`}),a(`progress-graph-circle-fill`,`
 stroke: var(--n-fill-color);
 transition:
 opacity .3s var(--n-bezier),
 stroke .3s var(--n-bezier),
 stroke-dasharray .3s var(--n-bezier);
 `,[f(`empty`,{opacity:0})]),a(`progress-graph-circle-rail`,`
 transition: stroke .3s var(--n-bezier);
 overflow: hidden;
 stroke: var(--n-rail-color);
 `)]),a(`progress-graph-line`,[f(`indicator-inside`,[a(`progress-graph-line-rail`,`
 height: 16px;
 line-height: 16px;
 border-radius: 10px;
 `,[a(`progress-graph-line-fill`,`
 height: inherit;
 border-radius: 10px;
 `),a(`progress-graph-line-indicator`,`
 background: #0000;
 white-space: nowrap;
 text-align: right;
 margin-left: 14px;
 margin-right: 14px;
 height: inherit;
 font-size: 12px;
 color: var(--n-text-color-line-inner);
 transition: color .3s var(--n-bezier);
 `)])]),f(`indicator-inside-label`,`
 height: 16px;
 display: flex;
 align-items: center;
 `,[a(`progress-graph-line-rail`,`
 flex: 1;
 transition: background-color .3s var(--n-bezier);
 `),a(`progress-graph-line-indicator`,`
 background: var(--n-fill-color);
 font-size: 12px;
 transform: translateZ(0);
 display: flex;
 vertical-align: middle;
 height: 16px;
 line-height: 16px;
 padding: 0 10px;
 border-radius: 10px;
 position: absolute;
 white-space: nowrap;
 color: var(--n-text-color-line-inner);
 transition:
 right .2s var(--n-bezier),
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 `)]),a(`progress-graph-line-rail`,`
 position: relative;
 overflow: hidden;
 height: var(--n-rail-height);
 border-radius: 5px;
 background-color: var(--n-rail-color);
 transition: background-color .3s var(--n-bezier);
 `,[a(`progress-graph-line-fill`,`
 background: var(--n-fill-color);
 position: relative;
 border-radius: 5px;
 height: inherit;
 width: 100%;
 max-width: 0%;
 transition:
 background-color .3s var(--n-bezier),
 max-width .2s var(--n-bezier);
 `,[f(`processing`,[u(`&::after`,`
 content: "";
 background-image: var(--n-line-bg-processing);
 animation: progress-processing-animation 2s var(--n-bezier) infinite;
 `)])])])])])]),u(`@keyframes progress-processing-animation`,`
 0% {
 position: absolute;
 left: 0;
 top: 0;
 bottom: 0;
 right: 100%;
 opacity: 1;
 }
 66% {
 position: absolute;
 left: 0;
 top: 0;
 bottom: 0;
 right: 0;
 opacity: 0;
 }
 100% {
 position: absolute;
 left: 0;
 top: 0;
 bottom: 0;
 right: 0;
 opacity: 0;
 }
 `)]),G=[`aria-valuenow`,`role`],K={...l.props,processing:Boolean,type:{type:String,default:`line`},gapDegree:Number,gapOffsetDegree:Number,status:{type:String,default:`default`},railColor:[String,Array],railStyle:[String,Array],color:[String,Array,Object],viewBoxWidth:{type:Number,default:100},strokeWidth:{type:Number,default:7},percentage:[Number,Array],unit:{type:String,default:`%`},showIndicator:{type:Boolean,default:!0},indicatorPosition:{type:String,default:`outside`},indicatorPlacement:{type:String,default:`outside`},indicatorTextColor:String,circleGap:{type:Number,default:1},height:Number,borderRadius:[String,Number],fillBorderRadius:[String,Number],offsetDegree:Number},q=v({name:`Progress`,props:K,setup(e){let n=x(()=>e.indicatorPlacement||e.indicatorPosition),r=x(()=>{if(e.gapDegree||e.gapDegree===0)return e.gapDegree;if(e.type===`dashboard`)return 75}),{mergedClsPrefixRef:a,inlineThemeDisabled:o}=y(e),s=l(`Progress`,`-progress`,W,T,e,a),c=x(()=>{let{status:n}=e,{common:{cubicBezierEaseInOut:r},self:{fontSize:i,fontSizeCircle:a,railColor:o,railHeight:c,iconSizeCircle:l,iconSizeLine:u,textColorCircle:d,textColorLineInner:f,textColorLineOuter:p,lineBgProcessing:m,fontWeightCircle:h,[t(`iconColor`,n)]:g,[t(`fillColor`,n)]:_}}=s.value;return{"--n-bezier":r,"--n-fill-color":_,"--n-font-size":i,"--n-font-size-circle":a,"--n-font-weight-circle":h,"--n-icon-color":g,"--n-icon-size-circle":l,"--n-icon-size-line":u,"--n-line-bg-processing":m,"--n-rail-color":o,"--n-rail-height":c,"--n-text-color-circle":d,"--n-text-color-line-inner":f,"--n-text-color-line-outer":p}}),u=o?i(`progress`,x(()=>e.status[0]),c,e):void 0;return{mergedClsPrefix:a,mergedIndicatorPlacement:n,gapDeg:r,cssVars:o?void 0:c,themeClass:u?.themeClass,onRender:u?.onRender}},render(){let{type:t,cssVars:n,indicatorTextColor:r,showIndicator:i,status:a,railColor:c,railStyle:l,color:u,percentage:f,viewBoxWidth:p,strokeWidth:h,mergedIndicatorPlacement:v,unit:y,borderRadius:b,fillBorderRadius:x,height:C,processing:w,circleGap:T,mergedClsPrefix:E,gapDeg:D,gapOffsetDegree:O,themeClass:k,$slots:A,onRender:j}=this;return j?.(),m(),S(`div`,{class:s([k,`${E}-progress`,`${E}-progress--${t}`,`${E}-progress--${a}`]),style:d(n),"aria-valuemax":100,"aria-valuemin":0,"aria-valuenow":f,role:t===`circle`||t===`line`||t===`dashboard`?`progressbar`:`none`},[t===`circle`||t===`dashboard`?(m(),_(N,{key:0,clsPrefix:E,status:a,showIndicator:i,indicatorTextColor:r,railColor:c,fillColor:u,railStyle:l,offsetDegree:this.offsetDegree,percentage:f,viewBoxWidth:p,strokeWidth:h,gapDegree:D===void 0?t===`dashboard`?75:0:D,gapOffsetDegree:O,unit:y},o(A),1032,[`clsPrefix`,`status`,`showIndicator`,`indicatorTextColor`,`railColor`,`fillColor`,`railStyle`,`offsetDegree`,`percentage`,`viewBoxWidth`,`strokeWidth`,`gapDegree`,`gapOffsetDegree`,`unit`])):(m(),S(e,{key:1},[t===`line`?(m(),_(F,{key:0,clsPrefix:E,status:a,showIndicator:i,indicatorTextColor:r,railColor:c,fillColor:u,railStyle:l,percentage:f,processing:w,indicatorPlacement:v,unit:y,fillBorderRadius:x,railBorderRadius:b,height:C},o(A),1032,[`clsPrefix`,`status`,`showIndicator`,`indicatorTextColor`,`railColor`,`fillColor`,`railStyle`,`percentage`,`processing`,`indicatorPlacement`,`unit`,`fillBorderRadius`,`railBorderRadius`,`height`])):(m(),S(e,{key:1},[t===`multiple-circle`?(m(),_(U,{key:0,clsPrefix:E,strokeWidth:h,railColor:c,fillColor:u,railStyle:l,viewBoxWidth:p,percentage:f,showIndicator:i,circleGap:T},o(A),1032,[`clsPrefix`,`strokeWidth`,`railColor`,`fillColor`,`railStyle`,`viewBoxWidth`,`percentage`,`showIndicator`,`circleGap`])):g(()=>null)],64))],64))],14,G)}});export{q as t};