import{Bt as e,F as t,Ft as n,K as r,Kt as i,Mn as a,P as o,Pt as s,Qn as c,Rt as l,Tn as u,Un as d,Y as f,_ as p,an as m,dn as h,et as g,in as _,rn as v,sn as y,un as b,v as x}from"./light-TBGKFU-e.js";import{I as S,ot as C}from"./request-BS4zoC18.js";import{t as w}from"./use-compitable-CpPvjT_b.js";import{n as T}from"./light-D3J98VY9.js";var E=s([s(`@keyframes spin-rotate`,`
 from {
 transform: rotate(0);
 }
 to {
 transform: rotate(360deg);
 }
 `),n(`spin-container`,`
 position: relative;
 `,[n(`spin-body`,`
 position: absolute;
 top: 50%;
 left: 50%;
 transform: translateX(-50%) translateY(-50%);
 `,[S()])]),n(`spin-body`,`
 display: inline-flex;
 align-items: center;
 justify-content: center;
 flex-direction: column;
 `),n(`spin`,`
 display: inline-flex;
 height: var(--n-size);
 width: var(--n-size);
 font-size: var(--n-size);
 color: var(--n-color);
 `,[l(`rotate`,`
 animation: spin-rotate 2s linear infinite;
 `)]),n(`spin-description`,`
 display: inline-block;
 font-size: var(--n-font-size);
 color: var(--n-text-color);
 transition: color .3s var(--n-bezier);
 margin-top: 8px;
 `),n(`spin-content`,`
 opacity: 1;
 transition: opacity .3s var(--n-bezier);
 pointer-events: all;
 `,[l(`spinning`,`
 user-select: none;
 -webkit-user-select: none;
 pointer-events: none;
 opacity: var(--n-opacity-spinning);
 `)])]),D={small:20,medium:18,large:16},O={...o.props,contentClass:String,contentStyle:[Object,String],description:String,size:{type:[String,Number],default:`medium`},show:{type:Boolean,default:!0},rotate:{type:Boolean,default:!0},spinning:{type:Boolean,validator:()=>!0,default:void 0},delay:Number,...x,strokeWidth:Number},k=h({name:`Spin`,props:O,slots:Object,setup(n){let{mergedClsPrefixRef:r,inlineThemeDisabled:i}=g(n),s=o(`Spin`,`-spin`,E,T,n,r),c=v(()=>{let{size:t}=n,{common:{cubicBezierEaseInOut:r},self:i}=s.value,{opacitySpinning:a,color:o,textColor:c}=i;return{"--n-bezier":r,"--n-opacity-spinning":a,"--n-size":typeof t==`number`?C(t):i[e(`size`,t)],"--n-color":o,"--n-text-color":c}}),l=i?t(`spin`,v(()=>{let{size:e}=n;return typeof e==`number`?String(e):e[0]}),c,n):void 0,u=w(n,[`spinning`,`show`]),f=d(!1);return a(e=>{let t;if(u.value){let{delay:r}=n;if(r){t=window.setTimeout(()=>{f.value=!0},r),e(()=>{clearTimeout(t)});return}}f.value=u.value}),{mergedClsPrefix:r,active:f,mergedStrokeWidth:v(()=>{let{strokeWidth:e}=n;if(e!==void 0)return e;let{size:t}=n;return D[typeof t==`number`?`medium`:t]}),cssVars:i?void 0:c,themeClass:l?.themeClass,onRender:l?.onRender}},render(){let{$slots:e,mergedClsPrefix:t,description:n}=this,a=e.icon&&this.rotate,o=(n||e.description)&&(u(),y(`div`,{class:r(`${t}-spin-description`)},[f(()=>n||e.description?.())],2)),s=e.icon?(u(),y(`div`,{key:1,class:r([`${t}-spin-body`,this.themeClass])},[_(`div`,{class:r([`${t}-spin`,a&&`${t}-spin--rotate`]),style:c(e.default?``:this.cssVars)},[f(()=>e.icon())],6),f(()=>o)],2)):(u(),y(`div`,{key:2,class:r([`${t}-spin-body`,this.themeClass])},[(u(),m(p,{clsPrefix:t,style:c(e.default?``:this.cssVars),stroke:this.stroke,"stroke-width":this.mergedStrokeWidth,radius:this.radius,scale:this.scale,class:r(`${t}-spin`)},null,8,[`clsPrefix`,`style`,`stroke`,`stroke-width`,`radius`,`scale`,`class`])),f(()=>o)],2));return this.onRender?.(),e.default?(u(),y(`div`,{key:3,class:r([`${t}-spin-container`,this.themeClass]),style:c(this.cssVars)},[_(`div`,{class:r([`${t}-spin-content`,this.active&&`${t}-spin-content--spinning`,this.contentClass]),style:c(this.contentStyle)},[f(()=>e.default?.())],6),b(i,{name:`fade-in-transition`},{default:()=>this.active?s:null},1024)],6)):s}});export{k as t};