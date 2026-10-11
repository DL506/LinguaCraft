import{A as e,En as t,F as n,Ft as r,J as i,K as a,Kt as o,Lt as s,Mn as c,P as l,Pn as u,Pt as d,Q as f,Qn as p,Rt as m,Tn as h,Un as g,Y as _,Yt as v,_n as y,an as b,dn as x,et as S,hn as C,jn as w,k as T,pn as E,qn as D,rn as O,sn as k,xn as A}from"./light-TBGKFU-e.js";import{$ as j,E as M,I as N,M as P,N as F,O as I,et as L,f as R,j as ee,nt as z,q as B,tt as V,u as H,v as U}from"./request-BS4zoC18.js";import{t as W}from"./use-merged-state-Cqr2carY.js";import{t as G}from"./format-length-BwSo_EUP.js";import{t as K}from"./light-DTE92uIj.js";var q=[`onMouseenter`,`onMouseleave`,`onMousedown`],J={key:1,role:`none`},Y=x({name:`NDrawerContent`,inheritAttrs:!1,props:{blockScroll:Boolean,show:{type:Boolean,default:void 0},displayDirective:{type:String,required:!0},placement:{type:String,required:!0},contentClass:String,contentStyle:[Object,String],nativeScrollbar:{type:Boolean,required:!0},scrollbarProps:Object,trapFocus:{type:Boolean,default:!0},autoFocus:{type:Boolean,default:!0},showMask:{type:[Boolean,String],required:!0},maxWidth:Number,maxHeight:Number,minWidth:Number,minHeight:Number,resizable:Boolean,onClickoutside:Function,onAfterLeave:Function,onAfterEnter:Function,onEsc:Function},setup(e){let n=g(!!e.show),r=g(null),i=C(z),a=0,o=``,s=null,l=g(!1),u=g(!1),d=O(()=>e.placement===`top`||e.placement===`bottom`),{mergedClsPrefixRef:f,mergedRtlRef:p}=S(e),m=T(`Drawer`,p,f),h=N,_=e=>{u.value=!0,a=d.value?e.clientY:e.clientX,o=document.body.style.cursor,document.body.style.cursor=d.value?`ns-resize`:`ew-resize`,document.body.addEventListener(`mousemove`,M),document.body.addEventListener(`mouseleave`,h),document.body.addEventListener(`mouseup`,N)},y=()=>{s!==null&&(window.clearTimeout(s),s=null),u.value?l.value=!0:s=window.setTimeout(()=>{l.value=!0},300)},b=()=>{s!==null&&(window.clearTimeout(s),s=null),l.value=!1},{doUpdateHeight:x,doUpdateWidth:E}=i,D=t=>{let{maxWidth:n}=e;if(n&&t>n)return n;let{minWidth:r}=e;return r&&t<r?r:t},k=t=>{let{maxHeight:n}=e;if(n&&t>n)return n;let{minHeight:r}=e;return r&&t<r?r:t};function M(t){if(u.value){if(d.value){let n=r.value?.offsetHeight||0,i=a-t.clientY;n+=e.placement===`bottom`?i:-i,n=k(n),x(n),a=t.clientY}else{let n=r.value?.offsetWidth||0,i=a-t.clientX;n+=e.placement===`right`?i:-i,n=D(n),E(n),a=t.clientX}}}function N(){u.value&&(a=0,u.value=!1,document.body.style.cursor=o,document.body.removeEventListener(`mousemove`,M),document.body.removeEventListener(`mouseup`,N),document.body.removeEventListener(`mouseleave`,h))}c(()=>{e.show&&(n.value=!0)}),w(()=>e.show,e=>{e||N()}),A(()=>{N()});let P=O(()=>{let{show:t}=e,n=[[v,t]];return e.showMask||n.push([F,e.onClickoutside,void 0,{capture:!0}]),n});function I(){n.value=!1,e.onAfterLeave?.()}return U(O(()=>e.blockScroll&&n.value)),t(V,r),t(j,null),t(L,null),{bodyRef:r,rtlEnabled:m,mergedClsPrefix:i.mergedClsPrefixRef,isMounted:i.isMountedRef,mergedTheme:i.mergedThemeRef,displayed:n,transitionName:O(()=>({right:`slide-in-from-right-transition`,left:`slide-in-from-left-transition`,top:`slide-in-from-top-transition`,bottom:`slide-in-from-bottom-transition`})[e.placement]),handleAfterLeave:I,bodyDirectives:P,handleMousedownResizeTrigger:_,handleMouseenterResizeTrigger:y,handleMouseleaveResizeTrigger:b,isDragging:u,isHoverOnResizeTrigger:l}},render(){let{$slots:e,mergedClsPrefix:t}=this;return this.displayDirective===`show`||this.displayed||this.show?u((h(),k(`div`,J,[(h(),b(I,{disabled:!this.showMask||!this.trapFocus,active:this.show,autoFocus:this.autoFocus,onEsc:this.onEsc},{default:()=>(h(),b(o,{name:this.transitionName,appear:this.isMounted,onAfterEnter:this.onAfterEnter,onAfterLeave:this.handleAfterLeave},{default:()=>u(E(`div`,y(this.$attrs,{role:`dialog`,ref:`bodyRef`,"aria-modal":`true`,class:[`${t}-drawer`,this.rtlEnabled&&`${t}-drawer--rtl`,`${t}-drawer--${this.placement}-placement`,this.isDragging&&`${t}-drawer--unselectable`,this.nativeScrollbar&&`${t}-drawer--native-scrollbar`]}),[this.resizable?(h(),k(`div`,{key:2,class:a([`${t}-drawer__resize-trigger`,(this.isDragging||this.isHoverOnResizeTrigger)&&`${t}-drawer__resize-trigger--hover`]),onMouseenter:this.handleMouseenterResizeTrigger,onMouseleave:this.handleMouseleaveResizeTrigger,onMousedown:this.handleMousedownResizeTrigger},null,42,q)):null,this.nativeScrollbar?(h(),k(`div`,{key:3,class:a([`${t}-drawer-content-wrapper`,this.contentClass]),style:p(this.contentStyle),role:`none`},[_(()=>e.default?.())],6)):(h(),b(M,y({key:4},this.scrollbarProps,{contentStyle:this.contentStyle,contentClass:[`${t}-drawer-content-wrapper`,this.contentClass],theme:this.mergedTheme.peers.Scrollbar,themeOverrides:this.mergedTheme.peerOverrides.Scrollbar}),i(e),1040,[`contentStyle`,`contentClass`,`theme`,`themeOverrides`]))]),this.bodyDirectives)},1032,[`name`,`appear`,`onAfterEnter`,`onAfterLeave`]))},1032,[`disabled`,`active`,`autoFocus`,`onEsc`]))])),[[v,this.displayDirective===`if`||this.displayed||this.show]]):null}}),{cubicBezierEaseIn:X,cubicBezierEaseOut:Z}=f;function Q({duration:e=`0.3s`,leaveDuration:t=`0.2s`,name:n=`slide-in-from-bottom`}={}){return[d(`&.${n}-transition-leave-active`,{transition:`transform ${t} ${X}`}),d(`&.${n}-transition-enter-active`,{transition:`transform ${e} ${Z}`}),d(`&.${n}-transition-enter-to`,{transform:`translateY(0)`}),d(`&.${n}-transition-enter-from`,{transform:`translateY(100%)`}),d(`&.${n}-transition-leave-from`,{transform:`translateY(0)`}),d(`&.${n}-transition-leave-to`,{transform:`translateY(100%)`})]}var{cubicBezierEaseIn:te,cubicBezierEaseOut:ne}=f;function re({duration:e=`0.3s`,leaveDuration:t=`0.2s`,name:n=`slide-in-from-left`}={}){return[d(`&.${n}-transition-leave-active`,{transition:`transform ${t} ${te}`}),d(`&.${n}-transition-enter-active`,{transition:`transform ${e} ${ne}`}),d(`&.${n}-transition-enter-to`,{transform:`translateX(0)`}),d(`&.${n}-transition-enter-from`,{transform:`translateX(-100%)`}),d(`&.${n}-transition-leave-from`,{transform:`translateX(0)`}),d(`&.${n}-transition-leave-to`,{transform:`translateX(-100%)`})]}var{cubicBezierEaseIn:ie,cubicBezierEaseOut:ae}=f;function oe({duration:e=`0.3s`,leaveDuration:t=`0.2s`,name:n=`slide-in-from-right`}={}){return[d(`&.${n}-transition-leave-active`,{transition:`transform ${t} ${ie}`}),d(`&.${n}-transition-enter-active`,{transition:`transform ${e} ${ae}`}),d(`&.${n}-transition-enter-to`,{transform:`translateX(0)`}),d(`&.${n}-transition-enter-from`,{transform:`translateX(100%)`}),d(`&.${n}-transition-leave-from`,{transform:`translateX(0)`}),d(`&.${n}-transition-leave-to`,{transform:`translateX(100%)`})]}var{cubicBezierEaseIn:se,cubicBezierEaseOut:ce}=f;function $({duration:e=`0.3s`,leaveDuration:t=`0.2s`,name:n=`slide-in-from-top`}={}){return[d(`&.${n}-transition-leave-active`,{transition:`transform ${t} ${se}`}),d(`&.${n}-transition-enter-active`,{transition:`transform ${e} ${ce}`}),d(`&.${n}-transition-enter-to`,{transform:`translateY(0)`}),d(`&.${n}-transition-enter-from`,{transform:`translateY(-100%)`}),d(`&.${n}-transition-leave-from`,{transform:`translateY(0)`}),d(`&.${n}-transition-leave-to`,{transform:`translateY(-100%)`})]}var le=d([r(`drawer`,`
 word-break: break-word;
 line-height: var(--n-line-height);
 position: absolute;
 pointer-events: all;
 box-shadow: var(--n-box-shadow);
 transition:
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 background-color: var(--n-color);
 color: var(--n-text-color);
 box-sizing: border-box;
 `,[oe(),re(),$(),Q(),m(`unselectable`,`
 user-select: none; 
 -webkit-user-select: none;
 `),m(`native-scrollbar`,[r(`drawer-content-wrapper`,`
 overflow: auto;
 height: 100%;
 `)]),s(`resize-trigger`,`
 position: absolute;
 background-color: #0000;
 transition: background-color .3s var(--n-bezier);
 `,[m(`hover`,`
 background-color: var(--n-resize-trigger-color-hover);
 `)]),r(`drawer-content-wrapper`,`
 box-sizing: border-box;
 `),r(`drawer-content`,`
 height: 100%;
 display: flex;
 flex-direction: column;
 `,[m(`native-scrollbar`,[r(`drawer-body-content-wrapper`,`
 height: 100%;
 overflow: auto;
 `)]),r(`drawer-body`,`
 flex: 1 0 0;
 overflow: hidden;
 `),r(`drawer-body-content-wrapper`,`
 box-sizing: border-box;
 padding: var(--n-body-padding);
 `),r(`drawer-header`,`
 font-weight: var(--n-title-font-weight);
 line-height: 1;
 font-size: var(--n-title-font-size);
 color: var(--n-title-text-color);
 padding: var(--n-header-padding);
 transition: border .3s var(--n-bezier);
 border-bottom: 1px solid var(--n-divider-color);
 border-bottom: var(--n-header-border-bottom);
 display: flex;
 justify-content: space-between;
 align-items: center;
 `,[s(`main`,`
 flex: 1;
 `),s(`close`,`
 margin-left: 6px;
 transition:
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 `)]),r(`drawer-footer`,`
 display: flex;
 justify-content: flex-end;
 border-top: var(--n-footer-border-top);
 transition: border .3s var(--n-bezier);
 padding: var(--n-footer-padding);
 `)]),m(`right-placement`,`
 top: 0;
 bottom: 0;
 right: 0;
 border-top-left-radius: var(--n-border-radius);
 border-bottom-left-radius: var(--n-border-radius);
 `,[s(`resize-trigger`,`
 width: 3px;
 height: 100%;
 top: 0;
 left: 0;
 transform: translateX(-1.5px);
 cursor: ew-resize;
 `)]),m(`left-placement`,`
 top: 0;
 bottom: 0;
 left: 0;
 border-top-right-radius: var(--n-border-radius);
 border-bottom-right-radius: var(--n-border-radius);
 `,[s(`resize-trigger`,`
 width: 3px;
 height: 100%;
 top: 0;
 right: 0;
 transform: translateX(1.5px);
 cursor: ew-resize;
 `)]),m(`top-placement`,`
 top: 0;
 left: 0;
 right: 0;
 border-bottom-left-radius: var(--n-border-radius);
 border-bottom-right-radius: var(--n-border-radius);
 `,[s(`resize-trigger`,`
 width: 100%;
 height: 3px;
 bottom: 0;
 left: 0;
 transform: translateY(1.5px);
 cursor: ns-resize;
 `)]),m(`bottom-placement`,`
 left: 0;
 bottom: 0;
 right: 0;
 border-top-left-radius: var(--n-border-radius);
 border-top-right-radius: var(--n-border-radius);
 `,[s(`resize-trigger`,`
 width: 100%;
 height: 3px;
 top: 0;
 left: 0;
 transform: translateY(-1.5px);
 cursor: ns-resize;
 `)])]),d(`body`,[d(`>`,[r(`drawer-container`,`
 position: fixed;
 `)])]),r(`drawer-container`,`
 position: relative;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 pointer-events: none;
 `,[d(`> *`,`
 pointer-events: all;
 `)]),r(`drawer-mask`,`
 background-color: rgba(0, 0, 0, .3);
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 `,[m(`invisible`,`
 background-color: rgba(0, 0, 0, 0)
 `),N({enterDuration:`0.2s`,leaveDuration:`0.2s`,enterCubicBezier:`var(--n-bezier-in)`,leaveCubicBezier:`var(--n-bezier-out)`})])]),ue=[`onClick`],de={...l.props,show:Boolean,width:[Number,String],height:[Number,String],placement:{type:String,default:`right`},maskClosable:{type:Boolean,default:!0},showMask:{type:[Boolean,String],default:!0},to:[String,Object],displayDirective:{type:String,default:`if`},nativeScrollbar:{type:Boolean,default:!0},zIndex:Number,onMaskClick:Function,scrollbarProps:Object,contentClass:String,contentStyle:[Object,String],trapFocus:{type:Boolean,default:!0},onEsc:Function,autoFocus:{type:Boolean,default:!0},closeOnEsc:{type:Boolean,default:!0},blockScroll:{type:Boolean,default:!0},maxWidth:Number,maxHeight:Number,minWidth:Number,minHeight:Number,resizable:Boolean,defaultWidth:{type:[Number,String],default:251},defaultHeight:{type:[Number,String],default:251},onUpdateWidth:[Function,Array],onUpdateHeight:[Function,Array],"onUpdate:width":[Function,Array],"onUpdate:height":[Function,Array],"onUpdate:show":[Function,Array],onUpdateShow:[Function,Array],onAfterEnter:Function,onAfterLeave:Function,drawerStyle:[String,Object],drawerClass:String,target:null,onShow:Function,onHide:Function},fe=x({name:`Drawer`,inheritAttrs:!1,props:de,setup(r){let{mergedClsPrefixRef:i,namespaceRef:a,inlineThemeDisabled:o}=S(r),s=e(),c=l(`Drawer`,`-drawer`,le,K,r,i),u=g(r.defaultWidth),d=g(r.defaultHeight),f=W(D(r,`width`),u),p=W(D(r,`height`),d),m=O(()=>{let{placement:e}=r;return e===`top`||e===`bottom`?``:G(f.value)}),h=O(()=>{let{placement:e}=r;return e===`left`||e===`right`?``:G(p.value)}),_=e=>{let{onUpdateWidth:t,"onUpdate:width":n}=r;t&&B(t,e),n&&B(n,e),u.value=e},v=e=>{let{onUpdateHeight:t,"onUpdate:width":n}=r;t&&B(t,e),n&&B(n,e),d.value=e},y=O(()=>[{width:m.value,height:h.value},r.drawerStyle||``]);function b(e){let{onMaskClick:t,maskClosable:n}=r;n&&T(!1),t&&t(e)}function x(e){b(e)}let C=H();function w(e){r.onEsc?.(),r.show&&r.closeOnEsc&&R(e)&&(C.value||T(!1))}function T(e){let{onHide:t,onUpdateShow:n,"onUpdate:show":i}=r;n&&B(n,e),i&&B(i,e),t&&!e&&B(t,e)}t(z,{isMountedRef:s,mergedThemeRef:c,mergedClsPrefixRef:i,doUpdateShow:T,doUpdateHeight:v,doUpdateWidth:_});let E=O(()=>{let{common:{cubicBezierEaseInOut:e,cubicBezierEaseIn:t,cubicBezierEaseOut:n},self:{color:r,textColor:i,boxShadow:a,lineHeight:o,headerPadding:s,footerPadding:l,borderRadius:u,bodyPadding:d,titleFontSize:f,titleTextColor:p,titleFontWeight:m,headerBorderBottom:h,footerBorderTop:g,closeIconColor:_,closeIconColorHover:v,closeIconColorPressed:y,closeColorHover:b,closeColorPressed:x,closeIconSize:S,closeSize:C,closeBorderRadius:w,resizableTriggerColorHover:T}}=c.value;return{"--n-line-height":o,"--n-color":r,"--n-border-radius":u,"--n-text-color":i,"--n-box-shadow":a,"--n-bezier":e,"--n-bezier-out":n,"--n-bezier-in":t,"--n-header-padding":s,"--n-body-padding":d,"--n-footer-padding":l,"--n-title-text-color":p,"--n-title-font-size":f,"--n-title-font-weight":m,"--n-header-border-bottom":h,"--n-footer-border-top":g,"--n-close-icon-color":_,"--n-close-icon-color-hover":v,"--n-close-icon-color-pressed":y,"--n-close-size":C,"--n-close-color-hover":b,"--n-close-color-pressed":x,"--n-close-icon-size":S,"--n-close-border-radius":w,"--n-resize-trigger-color-hover":T}}),k=o?n(`drawer`,void 0,E,r):void 0;return{mergedClsPrefix:i,namespace:a,mergedBodyStyle:y,handleOutsideClick:x,handleMaskClick:b,handleEsc:w,mergedTheme:c,cssVars:o?void 0:E,themeClass:k?.themeClass,onRender:k?.onRender,isMounted:s}},render(){let{mergedClsPrefix:e}=this;return h(),b(ee,{to:this.to,show:this.show},{default:()=>(this.onRender?.(),u((h(),k(`div`,{class:a([`${e}-drawer-container`,this.namespace,this.themeClass]),style:p(this.cssVars),role:`none`},[this.showMask?(h(),b(o,{key:0,name:`fade-in-transition`,appear:this.isMounted},{default:()=>this.show?(h(),k(`div`,{key:1,"aria-hidden":!0,class:a([`${e}-drawer-mask`,this.showMask===`transparent`&&`${e}-drawer-mask--invisible`]),onClick:this.handleMaskClick},null,10,ue)):null},1032,[`appear`])):_(()=>null),(h(),b(Y,y(this.$attrs,{class:[this.drawerClass,this.$attrs.class],style:[this.mergedBodyStyle,this.$attrs.style],blockScroll:this.blockScroll,contentStyle:this.contentStyle,contentClass:this.contentClass,placement:this.placement,scrollbarProps:this.scrollbarProps,show:this.show,displayDirective:this.displayDirective,nativeScrollbar:this.nativeScrollbar,onAfterEnter:this.onAfterEnter,onAfterLeave:this.onAfterLeave,trapFocus:this.trapFocus,autoFocus:this.autoFocus,resizable:this.resizable,maxHeight:this.maxHeight,minHeight:this.minHeight,maxWidth:this.maxWidth,minWidth:this.minWidth,showMask:this.showMask,onEsc:this.handleEsc,onClickoutside:this.handleOutsideClick}),i(this.$slots),1040,[`class`,`style`,`blockScroll`,`contentStyle`,`contentClass`,`placement`,`scrollbarProps`,`show`,`displayDirective`,`nativeScrollbar`,`onAfterEnter`,`onAfterLeave`,`trapFocus`,`autoFocus`,`resizable`,`maxHeight`,`minHeight`,`maxWidth`,`minWidth`,`showMask`,`onEsc`,`onClickoutside`]))],6)),[[P,{zIndex:this.zIndex,enabled:this.show}]]))},1032,[`to`,`show`])}});export{fe as t};