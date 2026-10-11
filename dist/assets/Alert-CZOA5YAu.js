import{Bt as e,C as t,D as n,E as r,F as i,Ft as a,H as o,K as s,Lt as c,M as l,P as u,Pt as d,R as f,Rt as p,S as m,T as h,Tn as g,Un as _,V as v,Y as y,_n as b,an as x,dn as S,et as C,in as w,k as T,rn as E,sn as D,w as O,x as k}from"./light-TBGKFU-e.js";import{B as A,H as j,at as M}from"./request-BS4zoC18.js";import{t as N}from"./_common-DnEpalBx.js";function P(e){let{lineHeight:t,borderRadius:n,fontWeightStrong:r,baseColor:i,dividerColor:a,actionColor:s,textColor1:c,textColor2:l,closeColorHover:u,closeColorPressed:d,closeIconColor:f,closeIconColorHover:p,closeIconColorPressed:m,infoColor:h,successColor:g,warningColor:_,errorColor:y,fontSize:b}=e;return{...N,fontSize:b,lineHeight:t,titleFontWeight:r,borderRadius:n,border:`1px solid ${a}`,color:s,titleTextColor:c,iconColor:l,contentTextColor:l,closeBorderRadius:n,closeColorHover:u,closeColorPressed:d,closeIconColor:f,closeIconColorHover:p,closeIconColorPressed:m,borderInfo:`1px solid ${o(i,v(h,{alpha:.25}))}`,colorInfo:o(i,v(h,{alpha:.08})),titleTextColorInfo:c,iconColorInfo:h,contentTextColorInfo:l,closeColorHoverInfo:u,closeColorPressedInfo:d,closeIconColorInfo:f,closeIconColorHoverInfo:p,closeIconColorPressedInfo:m,borderSuccess:`1px solid ${o(i,v(g,{alpha:.25}))}`,colorSuccess:o(i,v(g,{alpha:.08})),titleTextColorSuccess:c,iconColorSuccess:g,contentTextColorSuccess:l,closeColorHoverSuccess:u,closeColorPressedSuccess:d,closeIconColorSuccess:f,closeIconColorHoverSuccess:p,closeIconColorPressedSuccess:m,borderWarning:`1px solid ${o(i,v(_,{alpha:.33}))}`,colorWarning:o(i,v(_,{alpha:.08})),titleTextColorWarning:c,iconColorWarning:_,contentTextColorWarning:l,closeColorHoverWarning:u,closeColorPressedWarning:d,closeIconColorWarning:f,closeIconColorHoverWarning:p,closeIconColorPressedWarning:m,borderError:`1px solid ${o(i,v(y,{alpha:.25}))}`,colorError:o(i,v(y,{alpha:.08})),titleTextColorError:c,iconColorError:y,contentTextColorError:l,closeColorHoverError:u,closeColorPressedError:d,closeIconColorError:f,closeIconColorHoverError:p,closeIconColorPressedError:m}}var F={name:`Alert`,common:f,self:P},I=a(`alert`,`
 line-height: var(--n-line-height);
 border-radius: var(--n-border-radius);
 position: relative;
 transition: background-color .3s var(--n-bezier);
 background-color: var(--n-color);
 text-align: start;
 word-break: break-word;
`,[c(`border`,`
 border-radius: inherit;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 transition: border-color .3s var(--n-bezier);
 border: var(--n-border);
 pointer-events: none;
 `),p(`closable`,[a(`alert-body`,[c(`title`,`
 padding-right: 24px;
 `)])]),c(`icon`,{color:`var(--n-icon-color)`}),a(`alert-body`,{padding:`var(--n-padding)`},[c(`title`,{color:`var(--n-title-text-color)`}),c(`content`,{color:`var(--n-content-text-color)`})]),k({originalTransition:`transform .3s var(--n-bezier)`,enterToProps:{transform:`scale(1)`},leaveToProps:{transform:`scale(0.9)`}}),c(`icon`,`
 position: absolute;
 left: 0;
 top: 0;
 align-items: center;
 justify-content: center;
 display: flex;
 width: var(--n-icon-size);
 height: var(--n-icon-size);
 font-size: var(--n-icon-size);
 margin: var(--n-icon-margin);
 `),c(`close`,`
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 position: absolute;
 right: 0;
 top: 0;
 margin: var(--n-close-margin);
 `),p(`show-icon`,[a(`alert-body`,{paddingLeft:`calc(var(--n-icon-margin-left) + var(--n-icon-size) + var(--n-icon-margin-right))`})]),p(`right-adjust`,[a(`alert-body`,{paddingRight:`calc(var(--n-close-size) + var(--n-padding) + 2px)`})]),a(`alert-body`,`
 border-radius: var(--n-border-radius);
 transition: border-color .3s var(--n-bezier);
 `,[c(`title`,`
 transition: color .3s var(--n-bezier);
 font-size: 16px;
 line-height: 19px;
 font-weight: var(--n-title-font-weight);
 `,[d(`& +`,[c(`content`,{marginTop:`9px`})])]),c(`content`,{transition:`color .3s var(--n-bezier)`,fontSize:`var(--n-font-size)`})]),c(`icon`,{transition:`color .3s var(--n-bezier)`})]),L={...u.props,title:String,showIcon:{type:Boolean,default:!0},type:{type:String,default:`default`},bordered:{type:Boolean,default:!0},closable:Boolean,onClose:Function,onAfterLeave:Function,onAfterHide:Function},R=S({name:`Alert`,inheritAttrs:!1,props:L,slots:Object,setup(t){let{mergedClsPrefixRef:n,mergedBorderedRef:r,inlineThemeDisabled:a,mergedRtlRef:o}=C(t),s=u(`Alert`,`-alert`,I,F,t,n),c=T(`Alert`,o,n),l=E(()=>{let{common:{cubicBezierEaseInOut:n},self:r}=s.value,{fontSize:i,borderRadius:a,titleFontWeight:o,lineHeight:c,iconSize:l,iconMargin:u,iconMarginRtl:d,closeIconSize:f,closeBorderRadius:p,closeSize:m,closeMargin:h,closeMarginRtl:g,padding:_}=r,{type:v}=t,{left:y,right:b}=M(u);return{"--n-bezier":n,"--n-color":r[e(`color`,v)],"--n-close-icon-size":f,"--n-close-border-radius":p,"--n-close-color-hover":r[e(`closeColorHover`,v)],"--n-close-color-pressed":r[e(`closeColorPressed`,v)],"--n-close-icon-color":r[e(`closeIconColor`,v)],"--n-close-icon-color-hover":r[e(`closeIconColorHover`,v)],"--n-close-icon-color-pressed":r[e(`closeIconColorPressed`,v)],"--n-icon-color":r[e(`iconColor`,v)],"--n-border":r[e(`border`,v)],"--n-title-text-color":r[e(`titleTextColor`,v)],"--n-content-text-color":r[e(`contentTextColor`,v)],"--n-line-height":c,"--n-border-radius":a,"--n-font-size":i,"--n-title-font-weight":o,"--n-icon-size":l,"--n-icon-margin":u,"--n-icon-margin-rtl":d,"--n-close-size":m,"--n-close-margin":h,"--n-close-margin-rtl":g,"--n-padding":_,"--n-icon-margin-left":y,"--n-icon-margin-right":b}}),d=a?i(`alert`,E(()=>t.type[0]),l,t):void 0,f=_(!0),p=()=>{let{onAfterLeave:e,onAfterHide:n}=t;e&&e(),n&&n()};return{rtlEnabled:c,mergedClsPrefix:n,mergedBordered:r,visible:f,handleCloseClick:()=>{Promise.resolve(t.onClose?.()).then(e=>{e!==!1&&(f.value=!1)})},handleAfterLeave:()=>{p()},mergedTheme:s,cssVars:a?void 0:l,themeClass:d?.themeClass,onRender:d?.onRender}},render(){return this.onRender?.(),g(),x(m,{onAfterLeave:this.handleAfterLeave},{default:()=>{let{mergedClsPrefix:e,$slots:i}=this,a={class:[`${e}-alert`,this.themeClass,this.closable&&`${e}-alert--closable`,this.showIcon&&`${e}-alert--show-icon`,!this.title&&this.closable&&`${e}-alert--right-adjust`,this.rtlEnabled&&`${e}-alert--rtl`],style:this.cssVars,role:`alert`};return this.visible?(g(),D(`div`,b({key:1},b(this.$attrs,a)),[y(()=>this.closable&&(g(),x(n,{clsPrefix:e,class:s(`${e}-alert__close`),onClick:this.handleCloseClick},null,8,[`clsPrefix`,`class`,`onClick`]))),y(()=>this.bordered&&(g(),D(`div`,{class:s(`${e}-alert__border`)},null,2))),y(()=>this.showIcon&&(g(),D(`div`,{class:s(`${e}-alert__icon`),"aria-hidden":`true`},[y(()=>A(i.icon,()=>[(g(),x(l,{clsPrefix:e},{default:()=>{switch(this.type){case`success`:return g(),x(O,{key:3});case`info`:return g(),x(h,{key:4});case`warning`:return g(),x(t,{key:5});case`error`:return g(),x(r,{key:6});default:return null}}},1032,[`clsPrefix`]))]))],2))),w(`div`,{class:s([`${e}-alert-body`,this.mergedBordered&&`${e}-alert-body--bordered`])},[y(()=>j(i.header,t=>{let n=t||this.title;return n?(g(),D(`div`,{key:2,class:s(`${e}-alert-body__title`)},[y(()=>n)],2)):null})),y(()=>i.default&&(g(),D(`div`,{class:s(`${e}-alert-body__content`)},[y(()=>i.default())],2)))],2)],16)):null}},1032,[`onAfterLeave`])}});export{R as t};