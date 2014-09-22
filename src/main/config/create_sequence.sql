drop sequence s_cm_global;
create sequence s_cm_global start 1 ;
grant usage,select on sequence s_cm_global to tgx_jdbc; 
drop sequence s_im_global;
create sequence s_im_global start 1 ;
grant usage,select on sequence s_im_global to tgx_jdbc; 
drop sequence s_bind_global;
create sequence s_bind_global start 1 ;
grant usage,select on sequence s_bind_global to tgx_jdbc; 