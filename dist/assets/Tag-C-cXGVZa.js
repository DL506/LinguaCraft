import{Bt as e,D as t,En as n,F as r,Ft as i,K as a,Lt as o,P as s,Pt as c,Qn as l,R as u,Rt as d,Tn as f,Un as p,V as m,Y as h,an as g,dn as _,et as v,in as y,k as b,qn as x,rn as S,rt as C,sn as w,zt as T}from"./light-TBGKFU-e.js";import{H as E,T as D,at as O,q as k}from"./request-BS4zoC18.js";import{t as A}from"./_common-BWSN0c8x.js";function j(e){let{textColor2:t,primaryColorHover:n,primaryColorPressed:r,primaryColor:i,infoColor:a,successColor:o,warningColor:s,errorColor:c,baseColor:l,borderColor:u,opacityDisabled:d,tagColor:f,closeIconColor:p,closeIconColorHover:h,closeIconColorPressed:g,borderRadiusSmall:_,fontSizeMini:v,fontSizeTiny:y,fontSizeSmall:b,fontSizeMedium:x,heightMini:S,heightTiny:C,heightSmall:w,heightMedium:T,closeColorHover:E,closeColorPressed:D,buttonColor2Hover:O,buttonColor2Pressed:k,fontWeightStrong:j}=e;return{...A,closeBorderRadius:_,heightTiny:S,heightSmall:C,heightMedium:w,heightLarge:T,borderRadius:_,opacityDisabled:d,fontSizeTiny:v,fontSizeSmall:y,fontSizeMedium:b,fontSizeLarge:x,fontWeightStrong:j,textColorCheckable:t,textColorHoverCheckable:t,textColorPressedCheckable:t,textColorChecked:l,colorCheckable:`#0000`,colorHoverCheckable:O,colorPressedCheckable:k,colorChecked:i,colorCheckedHover:n,colorCheckedPressed:r,border:`1px solid ${u}`,textColor:t,color:f,colorBordered:`rgb(250, 250, 252)`,closeIconColor:p,closeIconColorHover:h,closeIconColorPressed:g,closeColorHover:E,closeColorPressed:D,borderPrimary:`1px solid ${m(i,{alpha:.3})}`,textColorPrimary:i,colorPrimary:m(i,{alpha:.12}),colorBorderedPrimary:m(i,{alpha:.1}),closeIconColorPrimary:i,closeIconColorHoverPrimary:i,closeIconColorPressedPrimary:i,closeColorHoverPrimary:m(i,{alpha:.12}),closeColorPressedPrimary:m(i,{alpha:.18}),borderInfo:`1px solid ${m(a,{alpha:.3})}`,textColorInfo:a,colorInfo:m(a,{alpha:.12}),colorBorderedInfo:m(a,{alpha:.1}),closeIconColorInfo:a,closeIconColorHoverInfo:a,closeIconColorPressedInfo:a,closeColorHoverInfo:m(a,{alpha:.12}),closeColorPressedInfo:m(a,{alpha:.18}),borderSuccess:`1px solid ${m(o,{alpha:.3})}`,textColorSuccess:o,colorSuccess:m(o,{alpha:.12}),colorBorderedSuccess:m(o,{alpha:.1}),closeIconColorSuccess:o,closeIconColorHoverSuccess:o,closeIconColorPressedSuccess:o,closeColorHoverSuccess:m(o,{alpha:.12}),closeColorPressedSuccess:m(o,{alpha:.18}),borderWarning:`1px solid ${m(s,{alpha:.35})}`,textColorWarning:s,colorWarning:m(s,{alpha:.15}),colorBorderedWarning:m(s,{alpha:.12}),closeIconColorWarning:s,closeIconColorHoverWarning:s,closeIconColorPressedWarning:s,closeColorHoverWarning:m(s,{alpha:.12}),closeColorPressedWarning:m(s,{alpha:.18}),borderError:`1px solid ${m(c,{alpha:.23})}`,textColorError:c,colorError:m(c,{alpha:.1}),colorBorderedError:m(c,{alpha:.08}),closeIconColorError:c,closeIconColorHoverError:c,closeIconColorPressedError:c,closeColorHoverError:m(c,{alpha:.12}),closeColorPressedError:m(c,{alpha:.18})}}var M={name:`Tag`,common:u,self:j},N={color:Object,type:{type:String,default:`default`},round:Boolean,size:String,closable:Boolean,disabled:{type:Boolean,default:void 0}},P=i(`tag`,`
 --n-close-margin: var(--n-close-margin-top) var(--n-close-margin-right) var(--n-close-margin-bottom) var(--n-close-margin-left);
 white-space: nowrap;
 position: relative;
 box-sizing: border-box;
 cursor: default;
 display: inline-flex;
 align-items: center;
 flex-wrap: nowrap;
 padding: var(--n-padding);
 border-radius: var(--n-border-radius);
 color: var(--n-text-color);
 background-color: var(--n-color);
 transition: 
 border-color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier),
 opacity .3s var(--n-bezier);
 line-height: 1;
 height: var(--n-height);
 font-size: var(--n-font-size);
`,[d(`strong`,`
 font-weight: var(--n-font-weight-strong);
 `),o(`border`,`
 pointer-events: none;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 border-radius: inherit;
 border: var(--n-border);
 transition: border-color .3s var(--n-bezier);
 `),o(`icon`,`
 display: flex;
 margin: 0 4px 0 0;
 color: var(--n-text-color);
 transition: color .3s var(--n-bezier);
 font-size: var(--n-avatar-size-override);
 `),o(`avatar`,`
 display: flex;
 margin: 0 6px 0 0;
 `),o(`close`,`
 margin: var(--n-close-margin);
 transition:
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 `),d(`round`,`
 padding: 0 calc(var(--n-height) / 3);
 border-radius: calc(var(--n-height) / 2);
 `,[o(`icon`,`
 margin: 0 4px 0 calc((var(--n-height) - 8px) / -2);
 `),o(`avatar`,`
 margin: 0 6px 0 calc((var(--n-height) - 8px) / -2);
 `),d(`closable`,`
 padding: 0 calc(var(--n-height) / 4) 0 calc(var(--n-height) / 3);
 `)]),d(`icon, avatar`,[d(`round`,`
 padding: 0 calc(var(--n-height) / 3) 0 calc(var(--n-height) / 2);
 `)]),d(`disabled`,`
 cursor: not-allowed !important;
 opacity: var(--n-opacity-disabled);
 `),d(`checkable`,`
 cursor: pointer;
 box-shadow: none;
 color: var(--n-text-color-checkable);
 background-color: var(--n-color-checkable);
 `,[T(`disabled`,[c(`&:hover`,`background-color: var(--n-color-hover-checkable);`,[T(`checked`,`color: var(--n-text-color-hover-checkable);`)]),c(`&:active`,`background-color: var(--n-color-pressed-checkable);`,[T(`checked`,`color: var(--n-text-color-pressed-checkable);`)])]),d(`checked`,`
 color: var(--n-text-color-checked);
 background-color: var(--n-color-checked);
 `,[T(`disabled`,[c(`&:hover`,`background-color: var(--n-color-checked-hover);`),c(`&:active`,`background-color: var(--n-color-checked-pressed);`)])])])]),F=[`onClick`,`onMouseenter`,`onMouseleave`],I={...s.props,...N,bordered:{type:Boolean,default:void 0},checked:Boolean,checkable:Boolean,strong:Boolean,triggerClickOnClose:Boolean,onClose:[Array,Function],onMouseenter:Function,onMouseleave:Function,"onUpdate:checked":Function,onUpdateChecked:Function,internalCloseFocusable:{type:Boolean,default:!0},internalCloseIsButtonTag:{type:Boolean,default:!0},onCheckedChange:Function},L=C(`n-tag`),R=_({name:`Tag`,props:I,slots:Object,setup(t){let i=p(null),{mergedBorderedRef:a,mergedClsPrefixRef:o,inlineThemeDisabled:c,mergedRtlRef:l,mergedComponentPropsRef:u}=v(t),d=S(()=>t.size||u?.value?.Tag?.size||`medium`),f=s(`Tag`,`-tag`,P,M,t,o);n(L,{roundRef:x(t,`round`)});function m(){if(!t.disabled&&t.checkable){let{checked:e,onCheckedChange:n,onUpdateChecked:r,"onUpdate:checked":i}=t;r&&r(!e),i&&i(!e),n&&n(!e)}}function h(e){if(t.triggerClickOnClose||e.stopPropagation(),!t.disabled){let{onClose:n}=t;n&&k(n,e)}}let g={setTextContent(e){let{value:t}=i;t&&(t.textContent=e)}},_=b(`Tag`,l,o),y=S(()=>{let{type:n,color:{color:r,textColor:i}={}}=t,o=d.value,{common:{cubicBezierEaseInOut:s},self:{padding:c,closeMargin:l,borderRadius:u,opacityDisabled:p,textColorCheckable:m,textColorHoverCheckable:h,textColorPressedCheckable:g,textColorChecked:_,colorCheckable:v,colorHoverCheckable:y,colorPressedCheckable:b,colorChecked:x,colorCheckedHover:S,colorCheckedPressed:C,closeBorderRadius:w,fontWeightStrong:T,[e(`colorBordered`,n)]:E,[e(`closeSize`,o)]:D,[e(`closeIconSize`,o)]:k,[e(`fontSize`,o)]:A,[e(`height`,o)]:j,[e(`color`,n)]:M,[e(`textColor`,n)]:N,[e(`border`,n)]:P,[e(`closeIconColor`,n)]:F,[e(`closeIconColorHover`,n)]:I,[e(`closeIconColorPressed`,n)]:L,[e(`closeColorHover`,n)]:R,[e(`closeColorPressed`,n)]:z}}=f.value,B=O(l);return{"--n-font-weight-strong":T,"--n-avatar-size-override":`calc(${j} - 8px)`,"--n-bezier":s,"--n-border-radius":u,"--n-border":P,"--n-close-icon-size":k,"--n-close-color-pressed":z,"--n-close-color-hover":R,"--n-close-border-radius":w,"--n-close-icon-color":F,"--n-close-icon-color-hover":I,"--n-close-icon-color-pressed":L,"--n-close-icon-color-disabled":F,"--n-close-margin-top":B.top,"--n-close-margin-right":B.right,"--n-close-margin-bottom":B.bottom,"--n-close-margin-left":B.left,"--n-close-size":D,"--n-color":r||(a.value?E:M),"--n-color-checkable":v,"--n-color-checked":x,"--n-color-checked-hover":S,"--n-color-checked-pressed":C,"--n-color-hover-checkable":y,"--n-color-pressed-checkable":b,"--n-font-size":A,"--n-height":j,"--n-opacity-disabled":p,"--n-padding":c,"--n-text-color":i||N,"--n-text-color-checkable":m,"--n-text-color-checked":_,"--n-text-color-hover-checkable":h,"--n-text-color-pressed-checkable":g}}),C=c?r(`tag`,S(()=>{let e=``,{type:n,color:{color:r,textColor:i}={}}=t;return e+=n[0],e+=d.value[0],r&&(e+=`a${D(r)}`),i&&(e+=`b${D(i)}`),a.value&&(e+=`c`),e}),y,t):void 0;return{...g,rtlEnabled:_,mergedClsPrefix:o,contentRef:i,mergedBordered:a,handleClick:m,handleCloseClick:h,cssVars:c?void 0:y,themeClass:C?.themeClass,onRender:C?.onRender}},render(){let{mergedClsPrefix:e,rtlEnabled:n,closable:r,color:{borderColor:i}={},round:o,onRender:s,$slots:c}=this;s?.();let u=E(c.avatar,t=>t&&(f(),w(`div`,{class:a(`${e}-tag__avatar`)},[h(()=>t)],2))),d=E(c.icon,t=>t&&(f(),w(`div`,{class:a(`${e}-tag__icon`)},[h(()=>t)],2)));return f(),w(`div`,{class:a([`${e}-tag`,this.themeClass,{[`${e}-tag--rtl`]:n,[`${e}-tag--strong`]:this.strong,[`${e}-tag--disabled`]:this.disabled,[`${e}-tag--checkable`]:this.checkable,[`${e}-tag--checked`]:this.checkable&&this.checked,[`${e}-tag--round`]:o,[`${e}-tag--avatar`]:u,[`${e}-tag--icon`]:d,[`${e}-tag--closable`]:r}]),style:l(this.cssVars),onClick:this.handleClick,onMouseenter:this.onMouseenter,onMouseleave:this.onMouseleave},[h(()=>d||u),y(`span`,{class:a(`${e}-tag__content`),ref:`contentRef`},[h(()=>this.$slots.default?.())],2),!this.checkable&&r?(f(),g(t,{key:0,clsPrefix:e,class:a(`${e}-tag__close`),disabled:this.disabled,onClick:this.handleCloseClick,focusable:this.internalCloseFocusable,round:o,isButtonTag:this.internalCloseIsButtonTag,absolute:!0},null,8,[`clsPrefix`,`class`,`disabled`,`onClick`,`focusable`,`round`,`isButtonTag`])):h(()=>null),!this.checkable&&this.mergedBordered?(f(),w(`div`,{key:2,class:a(`${e}-tag__border`),style:l({borderColor:i})},null,6)):h(()=>null)],46,F)}});export{L as n,R as t};